package com.hexaware.careassist.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hexaware.careassist.dto.InvoiceDTO;
import com.hexaware.careassist.entity.Claim;
import com.hexaware.careassist.entity.ClaimPayment;
import com.hexaware.careassist.entity.HealthcareProvider;
import com.hexaware.careassist.entity.Invoice;
import com.hexaware.careassist.entity.InvoicePayment;
import com.hexaware.careassist.entity.Patient;
import com.hexaware.careassist.exception.BusinessValidationException;
import com.hexaware.careassist.exception.ResourceNotFoundException;
import com.hexaware.careassist.repository.ClaimPaymentRepository;
import com.hexaware.careassist.repository.ClaimRepository;
import com.hexaware.careassist.repository.HealthcareProviderRepository;
import com.hexaware.careassist.repository.InvoicePaymentRepository;
import com.hexaware.careassist.repository.InvoiceRepository;
import com.hexaware.careassist.repository.PatientRepository;
import com.hexaware.careassist.service.IInvoiceService;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class InvoiceServiceImpl implements IInvoiceService {

    private static final Set<String> ALLOWED_MANUAL_STATUSES =
            Set.of("PENDING", "UNPAID", "OVERDUE", "CANCELLED");
    private static final BigDecimal MAX_MONEY_AMOUNT = new BigDecimal("9999999999.99");

    private final InvoiceRepository invoiceRepository;
    private final HealthcareProviderRepository providerRepository;
    private final PatientRepository patientRepository;
    private final ClaimRepository claimRepository;
    private final ClaimPaymentRepository claimPaymentRepository;
    private final InvoicePaymentRepository paymentRepository;

    @Override
    public InvoiceDTO generateInvoice(InvoiceDTO dto) {
        validateInvoiceDates(dto);
        validateInvoiceAmounts(dto);
        HealthcareProvider provider = getProvider(dto.getProviderId());
        validateProviderOwner(provider);

        Invoice invoice = toEntity(dto);
        invoice.setHealthcareProvider(provider);
        if (invoice.getCreatedAt() == null) {
            invoice.setCreatedAt(LocalDateTime.now());
        }
        if (invoice.getStatus() == null || invoice.getStatus().isBlank()) {
            invoice.setStatus("PENDING");
        } else if ("PAID".equals(normalize(invoice.getStatus()))) {
            throw new BusinessValidationException(
                    "status",
                    "A new invoice cannot be created as PAID. Payment status is set by the payment flow.");
        }
        calculateInvoiceTotals(invoice);
        validateCalculatedTotal(invoice);
        return toDTO(invoiceRepository.save(invoice));
    }

    @Override
    public InvoiceDTO updateInvoice(Integer invoiceId, InvoiceDTO dto) {
        Invoice invoice = getInvoiceEntity(invoiceId);
        validateProviderOwner(invoice.getHealthcareProvider());
        validateInvoiceDates(dto);
        validateInvoiceAmounts(dto);

        if (claimRepository.existsByInvoiceInvoiceId(invoiceId)
                || paymentRepository.existsByInvoiceInvoiceId(invoiceId)) {
            throw new BusinessValidationException(
                    "invoiceId",
                    "An invoice linked to a claim or payment cannot be financially edited.");
        }

        HealthcareProvider provider = getProvider(dto.getProviderId());
        validateProviderOwner(provider);

        invoice.setInvoiceNumber(dto.getInvoiceNumber().trim());
        invoice.setHealthcareProvider(provider);
        invoice.setPatient(getPatient(dto.getPatientId()));
        invoice.setInvoiceDate(dto.getInvoiceDate());
        invoice.setDueDate(dto.getDueDate());
        invoice.setConsultationFee(dto.getConsultationFee());
        invoice.setDiagnosticTestsFee(dto.getDiagnosticTestsFee());
        invoice.setDiagnosticScanFee(dto.getDiagnosticScanFee());
        invoice.setMedicationsFee(dto.getMedicationsFee());
        invoice.setTaxPercentage(dto.getTaxPercentage());
        String requestedStatus = normalize(dto.getStatus());
        if ("PAID".equals(requestedStatus)) {
            throw new BusinessValidationException(
                    "status",
                    "Invoices can become PAID only through insurance and patient payment processing.");
        }
        if (!ALLOWED_MANUAL_STATUSES.contains(requestedStatus)) {
            throw new BusinessValidationException(
                    "status",
                    "Status must be PENDING, UNPAID, OVERDUE, or CANCELLED.");
        }
        invoice.setStatus(requestedStatus);
        if (dto.getCreatedAt() != null) {
            invoice.setCreatedAt(dto.getCreatedAt());
        }
        calculateInvoiceTotals(invoice);
        validateCalculatedTotal(invoice);
        return toDTO(invoiceRepository.save(invoice));
    }

    @Override
    @Transactional(readOnly = true)
    public InvoiceDTO getInvoiceById(Integer invoiceId) {
        Invoice invoice = getInvoiceEntity(invoiceId);
        validateReadAccess(invoice);
        return toDTO(invoice);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvoiceDTO> getInvoicesByPatientId(Integer patientId) {
        return invoiceRepository.findByPatientPatientId(patientId).stream()
                .filter(this::canCurrentUserRead)
                .map(this::toDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvoiceDTO> getMyInvoices(String authenticatedEmail) {
        Patient patient = patientRepository
                .findTopByAppUserEmailIgnoreCaseOrderByPatientIdDesc(authenticatedEmail)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Patient profile was not found for the logged-in account."));
        return invoiceRepository.findByPatientPatientId(patient.getPatientId()).stream()
                .map(this::toDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvoiceDTO> getInvoicesByProviderId(Integer providerId) {
        HealthcareProvider provider = getProvider(providerId);
        validateProviderOwner(provider);
        return invoiceRepository.findByHealthcareProviderProviderId(providerId).stream()
                .map(this::toDTO)
                .toList();
    }

    @Override
    public InvoiceDTO updateInvoiceStatus(Integer invoiceId, String status) {
        Invoice invoice = getInvoiceEntity(invoiceId);
        validateProviderOwner(invoice.getHealthcareProvider());

        String normalizedStatus = normalize(status);
        if ("PAID".equals(normalizedStatus)) {
            throw new BusinessValidationException(
                    "status",
                    "Invoices can become PAID only through insurance and patient payment processing.");
        }
        if (!ALLOWED_MANUAL_STATUSES.contains(normalizedStatus)) {
            throw new BusinessValidationException(
                    "status",
                    "Status must be PENDING, UNPAID, OVERDUE, or CANCELLED.");
        }
        if (paymentRepository.existsByInvoiceInvoiceId(invoiceId)) {
            throw new BusinessValidationException(
                    "invoiceId",
                    "A paid invoice status cannot be changed manually.");
        }
        if ("CANCELLED".equals(normalizedStatus)
                && claimRepository.existsByInvoiceInvoiceId(invoiceId)) {
            throw new BusinessValidationException(
                    "invoiceId",
                    "An invoice linked to a claim cannot be cancelled.");
        }
        invoice.setStatus(normalizedStatus);
        return toDTO(invoiceRepository.save(invoice));
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvoiceDTO> getAllInvoices() {
        return invoiceRepository.findAll().stream().map(this::toDTO).toList();
    }

    @Override
    public void deleteInvoice(Integer invoiceId) {
        Invoice invoice = getInvoiceEntity(invoiceId);
        if (claimRepository.existsByInvoiceInvoiceId(invoiceId)
                || paymentRepository.existsByInvoiceInvoiceId(invoiceId)) {
            throw new BusinessValidationException(
                    "invoiceId",
                    "An invoice linked to a claim or payment cannot be deleted.");
        }
        invoiceRepository.delete(invoice);
    }

    private void validateInvoiceDates(InvoiceDTO dto) {
        if (dto.getDueDate().isBefore(dto.getInvoiceDate())) {
            throw new BusinessValidationException(
                    "dueDate",
                    "Due date cannot be before invoice date.");
        }
    }

    private void validateInvoiceAmounts(InvoiceDTO dto) {
        validateNonNegativeMoney(dto.getConsultationFee(), "consultationFee", "Consultation fee");
        validateNonNegativeMoney(dto.getDiagnosticTestsFee(), "diagnosticTestsFee", "Diagnostic tests fee");
        validateNonNegativeMoney(dto.getDiagnosticScanFee(), "diagnosticScanFee", "Diagnostic scan fee");
        validateNonNegativeMoney(dto.getMedicationsFee(), "medicationsFee", "Medications fee");

        BigDecimal taxPercentage = dto.getTaxPercentage();
        if (taxPercentage == null
                || taxPercentage.compareTo(BigDecimal.ZERO) < 0
                || taxPercentage.compareTo(new BigDecimal("100")) > 0) {
            throw new BusinessValidationException(
                    "taxPercentage",
                    "Tax percentage must be between 0 and 100.");
        }
        if (Math.max(0, taxPercentage.stripTrailingZeros().scale()) > 2) {
            throw new BusinessValidationException(
                    "taxPercentage",
                    "Tax percentage can have at most 2 decimal places.");
        }
    }

    private void validateNonNegativeMoney(BigDecimal amount, String field, String label) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessValidationException(field, label + " cannot be negative.");
        }
        if (amount.compareTo(MAX_MONEY_AMOUNT) > 0) {
            throw new BusinessValidationException(field, label + " cannot exceed " + MAX_MONEY_AMOUNT + ".");
        }
        if (Math.max(0, amount.stripTrailingZeros().scale()) > 2) {
            throw new BusinessValidationException(field, label + " can have at most 2 decimal places.");
        }
    }

    private void validateCalculatedTotal(Invoice invoice) {
        BigDecimal total = safeAmount(invoice.getTotalAmount());
        if (total.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessValidationException(
                    "totalAmount",
                    "Invoice total must be greater than 0.");
        }
        if (total.compareTo(MAX_MONEY_AMOUNT) > 0) {
            throw new BusinessValidationException(
                    "totalAmount",
                    "Invoice total cannot exceed " + MAX_MONEY_AMOUNT + ".");
        }
    }

    private Invoice getInvoiceEntity(Integer invoiceId) {
        return invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Invoice not found with id: " + invoiceId));
    }

    private HealthcareProvider getProvider(Integer providerId) {
        return providerRepository.findById(providerId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Healthcare provider not found with id: " + providerId));
    }

    private Patient getPatient(Integer patientId) {
        return patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Patient not found with id: " + patientId));
    }

    private void calculateInvoiceTotals(Invoice invoice) {
        BigDecimal billAmount = safeAmount(invoice.getConsultationFee())
                .add(safeAmount(invoice.getDiagnosticTestsFee()))
                .add(safeAmount(invoice.getDiagnosticScanFee()))
                .add(safeAmount(invoice.getMedicationsFee()));

        BigDecimal taxPercentage = safeAmount(invoice.getTaxPercentage());
        BigDecimal taxAmount = billAmount.multiply(taxPercentage)
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        invoice.setTaxAmount(taxAmount);
        invoice.setTotalAmount(billAmount.add(taxAmount));
    }

    private InvoiceDTO toDTO(Invoice invoice) {
        InvoiceDTO dto = new InvoiceDTO();
        dto.setInvoiceId(invoice.getInvoiceId());
        dto.setInvoiceNumber(invoice.getInvoiceNumber());
        dto.setProviderId(invoice.getHealthcareProvider().getProviderId());
        dto.setProviderName(invoice.getHealthcareProvider().getProviderName());
        dto.setPatientId(invoice.getPatient().getPatientId());
        dto.setPatientName(invoice.getPatient().getFullName());
        dto.setInvoiceDate(invoice.getInvoiceDate());
        dto.setDueDate(invoice.getDueDate());
        dto.setConsultationFee(invoice.getConsultationFee());
        dto.setDiagnosticTestsFee(invoice.getDiagnosticTestsFee());
        dto.setDiagnosticScanFee(invoice.getDiagnosticScanFee());
        dto.setMedicationsFee(invoice.getMedicationsFee());
        dto.setTaxPercentage(invoice.getTaxPercentage());
        dto.setTaxAmount(invoice.getTaxAmount());
        dto.setTotalAmount(invoice.getTotalAmount());
        dto.setStatus(invoice.getStatus());
        dto.setCreatedAt(invoice.getCreatedAt());

        Optional<Claim> latestClaim = claimRepository
                .findFirstByInvoiceInvoiceIdOrderByClaimIdDesc(invoice.getInvoiceId());

        Claim claim = latestClaim.orElse(null);
        ClaimPayment insurancePayment = null;
        if (claim != null) {
            dto.setClaimId(claim.getClaimId());
            dto.setClaimStatus(claim.getStatus());
            dto.setClaimAmount(claim.getClaimAmount());
            dto.setApprovedAmount(effectiveApprovedAmount(claim));

            if (claim.getPatientInsurance() != null
                    && claim.getPatientInsurance().getInsurancePlan() != null) {
                BigDecimal coverage = safeAmount(
                        claim.getPatientInsurance().getInsurancePlan().getCoverageAmount());
                BigDecimal approvedUsed = safeAmount(
                        claimRepository.sumApprovedAmountByEnrollmentId(
                                claim.getPatientInsurance().getEnrollmentId()));
                dto.setPlanCoverageAmount(coverage);
                dto.setRemainingPlanCoverage(
                        coverage.subtract(approvedUsed).max(BigDecimal.ZERO));
            }

            insurancePayment = claimPaymentRepository.findByClaimClaimId(claim.getClaimId())
                    .orElse(null);
            if (insurancePayment != null) {
                dto.setInsurancePaymentId(insurancePayment.getPaymentId());
                dto.setInsurancePaymentDate(insurancePayment.getPaymentDate());
                dto.setInsurancePaidAmount(insurancePayment.getPaymentAmount());
                dto.setInsurancePaymentMode(insurancePayment.getPaymentMode());
                dto.setInsuranceTransactionReference(insurancePayment.getTransactionReference());
                dto.setInsurancePaymentProcessed(true);
            }
        }

        Optional<InvoicePayment> patientPayment = paymentRepository
                .findByInvoiceInvoiceId(invoice.getInvoiceId());
        patientPayment.ifPresent(value -> {
            dto.setPaymentId(value.getPaymentId());
            dto.setPaymentDate(value.getPaymentDate());
            dto.setPaymentAmount(value.getPaymentAmount());
            dto.setPaymentMethod(value.getPaymentMethod());
            dto.setTransactionReference(value.getTransactionReference());
            dto.setPaymentStatus(value.getPaymentStatus());
            dto.setPatientPaidAmount(value.getPaymentAmount());
        });

        BigDecimal total = safeAmount(invoice.getTotalAmount());
        BigDecimal insurancePaid = insurancePayment == null
                ? BigDecimal.ZERO
                : safeAmount(insurancePayment.getPaymentAmount());
        BigDecimal patientPaid = patientPayment
                .map(InvoicePayment::getPaymentAmount)
                .map(this::safeAmount)
                .orElse(BigDecimal.ZERO);
        BigDecimal remaining = total.subtract(insurancePaid).subtract(patientPaid).max(BigDecimal.ZERO);

        dto.setInsurancePaidAmount(insurancePaid);
        dto.setPatientPaidAmount(patientPaid);
        dto.setRemainingAmount(remaining);
        applyPaymentEligibility(dto, claim, insurancePayment, patientPayment.orElse(null));
        return dto;
    }

    private void applyPaymentEligibility(
            InvoiceDTO dto,
            Claim claim,
            ClaimPayment insurancePayment,
            InvoicePayment patientPayment) {

        String invoiceStatus = normalize(dto.getStatus());
        String claimStatus = claim == null ? "" : normalize(claim.getStatus());
        BigDecimal remaining = safeAmount(dto.getRemainingAmount());

        if (remaining.compareTo(BigDecimal.ZERO) == 0
                || patientPayment != null
                || "PAID".equals(invoiceStatus)) {
            dto.setPaymentEligible(false);
            dto.setPaymentEligibilityReason("Invoice is fully paid");
            return;
        }
        if ("CANCELLED".equals(invoiceStatus)) {
            dto.setPaymentEligible(false);
            dto.setPaymentEligibilityReason("Invoice is cancelled");
            return;
        }

        boolean unpaid = "PENDING".equals(invoiceStatus)
                || "OVERDUE".equals(invoiceStatus)
                || "UNPAID".equals(invoiceStatus);
        if (!unpaid) {
            dto.setPaymentEligible(false);
            dto.setPaymentEligibilityReason("Invoice is not payable in its current status");
            return;
        }

        if (claim == null) {
            dto.setPaymentEligible(true);
            dto.setPaymentEligibilityReason(
                    "No insurance claim is active. You can pay the full invoice amount directly.");
            return;
        }

        if ("PENDING".equals(claimStatus)
                || "SUBMITTED".equals(claimStatus)
                || "UNDER_REVIEW".equals(claimStatus)) {
            dto.setPaymentEligible(false);
            dto.setPaymentEligibilityReason(
                    "Insurance claim review is in progress. Wait for the insurer's decision before paying.");
            return;
        }

        if ("APPROVED".equals(claimStatus)) {
            if (insurancePayment == null) {
                dto.setPaymentEligible(false);
                dto.setPaymentEligibilityReason(
                        "Waiting for the insurance company to process the approved amount");
                return;
            }
            dto.setPaymentEligible(true);
            dto.setPaymentEligibilityReason(
                    "Insurance paid its approved amount. Pay the remaining balance.");
            return;
        }

        if ("REJECTED".equals(claimStatus)
                || "DENIED".equals(claimStatus)
                || "CANCELLED".equals(claimStatus)
                || "WITHDRAWN".equals(claimStatus)) {
            dto.setPaymentEligible(true);
            dto.setPaymentEligibilityReason(
                    "The insurance claim will not pay this invoice. You can pay the full remaining amount directly.");
            return;
        }

        dto.setPaymentEligible(false);
        dto.setPaymentEligibilityReason("Claim status is " + claimStatus);
    }

    private BigDecimal effectiveApprovedAmount(Claim claim) {
        if (claim == null || !"APPROVED".equalsIgnoreCase(claim.getStatus())) {
            return BigDecimal.ZERO;
        }
        return claim.getApprovedAmount() == null
                ? safeAmount(claim.getClaimAmount())
                : claim.getApprovedAmount();
    }

    private void validateReadAccess(Invoice invoice) {
        if (!canCurrentUserRead(invoice)) {
            throw new AccessDeniedException("You are not allowed to view this invoice.");
        }
    }

    private boolean canCurrentUserRead(Invoice invoice) {
        Authentication authentication = currentAuthentication();
        if (authentication == null || hasRole(authentication, "ROLE_ADMIN")) {
            return true;
        }

        String email = authentication.getName();
        if (hasRole(authentication, "ROLE_PATIENT")) {
            return invoice.getPatient() != null
                    && invoice.getPatient().getAppUser() != null
                    && email.equalsIgnoreCase(invoice.getPatient().getAppUser().getEmail());
        }
        if (hasRole(authentication, "ROLE_PROVIDER")) {
            return invoice.getHealthcareProvider() != null
                    && invoice.getHealthcareProvider().getAppUser() != null
                    && email.equalsIgnoreCase(invoice.getHealthcareProvider().getAppUser().getEmail());
        }
        return false;
    }

    private void validateProviderOwner(HealthcareProvider provider) {
        Authentication authentication = currentAuthentication();
        if (authentication == null || hasRole(authentication, "ROLE_ADMIN")) {
            return;
        }
        boolean ownsProvider = hasRole(authentication, "ROLE_PROVIDER")
                && provider != null
                && provider.getAppUser() != null
                && authentication.getName().equalsIgnoreCase(provider.getAppUser().getEmail());
        if (!ownsProvider) {
            throw new AccessDeniedException("Providers can manage only their own invoices.");
        }
    }

    private boolean hasRole(Authentication authentication, String role) {
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> role.equals(authority.getAuthority()));
    }

    private Authentication currentAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return null;
        }
        return authentication;
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
    }

    private BigDecimal safeAmount(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private Invoice toEntity(InvoiceDTO dto) {
        Invoice invoice = new Invoice();
        invoice.setInvoiceId(dto.getInvoiceId());
        invoice.setInvoiceNumber(dto.getInvoiceNumber().trim());
        invoice.setHealthcareProvider(getProvider(dto.getProviderId()));
        invoice.setPatient(getPatient(dto.getPatientId()));
        invoice.setInvoiceDate(dto.getInvoiceDate());
        invoice.setDueDate(dto.getDueDate());
        invoice.setConsultationFee(dto.getConsultationFee());
        invoice.setDiagnosticTestsFee(dto.getDiagnosticTestsFee());
        invoice.setDiagnosticScanFee(dto.getDiagnosticScanFee());
        invoice.setMedicationsFee(dto.getMedicationsFee());
        invoice.setTaxPercentage(dto.getTaxPercentage());
        invoice.setStatus(dto.getStatus());
        invoice.setCreatedAt(dto.getCreatedAt());
        return invoice;
    }
}
