package com.hexaware.careassist.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.hexaware.careassist.dto.ClaimDTO;
import com.hexaware.careassist.dto.ClaimDocumentDTO;
import com.hexaware.careassist.entity.Claim;
import com.hexaware.careassist.entity.InsuranceCompany;
import com.hexaware.careassist.entity.Invoice;
import com.hexaware.careassist.entity.Patient;
import com.hexaware.careassist.entity.PatientInsurance;
import com.hexaware.careassist.exception.BusinessValidationException;
import com.hexaware.careassist.exception.ResourceNotFoundException;
import com.hexaware.careassist.repository.ClaimDocumentRepository;
import com.hexaware.careassist.repository.ClaimPaymentRepository;
import com.hexaware.careassist.repository.ClaimRepository;
import com.hexaware.careassist.repository.InsuranceCompanyRepository;
import com.hexaware.careassist.repository.InvoicePaymentRepository;
import com.hexaware.careassist.repository.InvoiceRepository;
import com.hexaware.careassist.repository.PatientInsuranceRepository;
import com.hexaware.careassist.repository.PatientRepository;
import com.hexaware.careassist.service.IClaimDocumentService;
import com.hexaware.careassist.service.IClaimService;
import com.hexaware.careassist.service.IMailService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ClaimServiceImpl implements IClaimService {

    private static final Set<String> ACTIVE_CLAIM_STATUSES = Set.of("PENDING", "APPROVED");
    private static final Set<String> CLAIMABLE_INVOICE_STATUSES = Set.of("PENDING", "UNPAID", "OVERDUE");
    private static final BigDecimal MAX_MONEY_AMOUNT = new BigDecimal("9999999999.99");

    private final ClaimRepository claimRepository;
    private final ClaimPaymentRepository claimPaymentRepository;
    private final ClaimDocumentRepository claimDocumentRepository;
    private final PatientRepository patientRepository;
    private final InvoiceRepository invoiceRepository;
    private final InvoicePaymentRepository invoicePaymentRepository;
    private final InsuranceCompanyRepository companyRepository;
    private final PatientInsuranceRepository patientInsuranceRepository;
    private final IClaimDocumentService claimDocumentService;
    private final IMailService mailService;

    @Override
    public ClaimDTO submitClaim(ClaimDTO dto) {
        Claim savedClaim = createValidatedClaim(dto);
        sendClaimSubmittedEmails(savedClaim);
        return claimToDTO(savedClaim);
    }

    @Override
    public ClaimDTO submitClaimWithDocuments(ClaimDTO dto, List<MultipartFile> files) {
        if (files == null || files.stream().noneMatch(file -> file != null && !file.isEmpty())) {
            throw new BusinessValidationException("documents", "At least one medical document is required.");
        }

        Claim savedClaim = createValidatedClaim(dto);
        claimDocumentService.storeDocuments(savedClaim.getClaimId(), files);
        sendClaimSubmittedEmails(savedClaim);
        return claimToDTO(savedClaim);
    }

    private Claim createValidatedClaim(ClaimDTO dto) {
        Patient patient = getPatient(dto.getPatientId());
        Invoice invoice = getInvoice(dto.getInvoiceId());
        InsuranceCompany company = getCompany(dto.getCompanyId());
        PatientInsurance patientInsurance = getPatientInsurance(dto.getEnrollmentId());

        validateCoverageAndRelationships(dto, patient, invoice, company, patientInsurance);
        validateSubmitterOwnership(patient, invoice);

        Claim claim = new Claim();
        claim.setPatient(patient);
        claim.setInvoice(invoice);
        claim.setInsuranceCompany(company);
        claim.setPatientInsurance(patientInsurance);
        claim.setDiagnosis(dto.getDiagnosis().trim());
        claim.setTreatment(dto.getTreatment().trim());
        claim.setDateOfService(dto.getDateOfService());
        claim.setClaimAmount(dto.getClaimAmount());
        claim.setApprovedAmount(null);
        claim.setSubmissionDate(LocalDateTime.now());
        claim.setApprovalDate(null);
        claim.setStatus("PENDING");
        claim.setRejectionReason(null);

        Claim saved = claimRepository.save(claim);
        log.info(
                "Claim submitted claimId={} patientId={} invoiceId={} enrollmentId={} amount={}",
                saved.getClaimId(),
                patient.getPatientId(),
                invoice.getInvoiceId(),
                patientInsurance.getEnrollmentId(),
                saved.getClaimAmount());
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public ClaimDTO getClaimById(Integer claimId) {
        Claim claim = getClaimEntity(claimId);
        validateReadAccess(claim);
        return claimToDTO(claim);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClaimDTO> getClaimsByPatientId(Integer patientId) {
        return claimRepository.findByPatientPatientId(patientId).stream()
                .filter(this::canCurrentUserRead)
                .map(this::claimToDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClaimDTO> getClaimsByInsuranceCompanyId(Integer companyId) {
        return claimRepository.findByInsuranceCompanyCompanyId(companyId).stream()
                .filter(this::canCurrentUserRead)
                .map(this::claimToDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClaimDTO> getActionableClaimsByInsuranceCompanyId(Integer companyId) {
        InsuranceCompany company = getCompany(companyId);
        validateInsuranceCompanyAccess(company);
        return claimRepository.findActionableClaimsByCompanyId(companyId).stream()
                .map(this::claimToDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClaimDTO> getAllClaims() {
        Authentication authentication = currentAuthentication();
        if (authentication != null && authentication.getAuthorities().stream()
                .noneMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()))) {
            throw new AccessDeniedException("Only administrators can view all claims.");
        }
        return claimRepository.findAll().stream()
                .map(this::claimToDTO)
                .toList();
    }

    @Override
    public ClaimDTO approveClaim(Integer claimId) {
        Claim claim = getClaimEntity(claimId);
        return approveClaim(claimId, claim.getClaimAmount());
    }

    @Override
    public ClaimDTO approveClaim(Integer claimId, BigDecimal approvedAmount) {
        Claim claim = getClaimEntity(claimId);
        if (!"PENDING".equalsIgnoreCase(claim.getStatus())) {
            throw new BusinessValidationException("status", "Only pending claims can be approved.");
        }

        validateInsuranceDecisionOwner(claim);
        validateInvoiceCanReceiveInsurancePayment(claim.getInvoice());
        validateApprovedAmount(claim, approvedAmount);
        lockPolicyAndValidateRemainingCoverage(claim, approvedAmount);

        claim.setApprovedAmount(approvedAmount);
        claim.setStatus("APPROVED");
        claim.setApprovalDate(LocalDateTime.now());
        claim.setRejectionReason(null);
        Claim savedClaim = claimRepository.save(claim);

        sendClaimStatusEmail(
                savedClaim,
                "approved",
                "Your claim has been approved for " + approvedAmount
                        + ". The insurance company must process this amount before you pay the remaining invoice balance.");
        log.info("Claim approved claimId={} requestedAmount={} approvedAmount={}",
                claimId, claim.getClaimAmount(), approvedAmount);
        return claimToDTO(savedClaim);
    }

    @Override
    public ClaimDTO rejectClaim(Integer claimId, String rejectionReason) {
        if (rejectionReason == null || rejectionReason.isBlank()) {
            throw new BusinessValidationException("rejectionReason", "Rejection reason is required.");
        }
        String normalizedReason = rejectionReason.trim();
        if (normalizedReason.length() < 5 || normalizedReason.length() > 255) {
            throw new BusinessValidationException(
                    "rejectionReason",
                    "Rejection reason must be between 5 and 255 characters.");
        }

        Claim claim = getClaimEntity(claimId);
        if (!"PENDING".equalsIgnoreCase(claim.getStatus())) {
            throw new BusinessValidationException("status", "Only pending claims can be rejected.");
        }

        validateInsuranceDecisionOwner(claim);
        claim.setStatus("REJECTED");
        claim.setApprovedAmount(null);
        claim.setApprovalDate(null);
        claim.setRejectionReason(normalizedReason);
        Claim savedClaim = claimRepository.save(claim);

        sendClaimStatusEmail(
                savedClaim,
                "rejected",
                "Your claim has been rejected. Reason: " + savedClaim.getRejectionReason());
        log.info("Claim rejected claimId={} reason={}", claimId, savedClaim.getRejectionReason());
        return claimToDTO(savedClaim);
    }

    @Override
    public void deleteClaim(Integer claimId) {
        Claim claim = getClaimEntity(claimId);
        validateSubmitterOwnership(claim.getPatient(), claim.getInvoice());
        if ("APPROVED".equalsIgnoreCase(claim.getStatus())
                || claimPaymentRepository.existsByClaimClaimId(claimId)) {
            throw new BusinessValidationException(
                    "claimId",
                    "An approved or paid claim is part of the financial record and cannot be deleted.");
        }
        List<ClaimDocumentDTO> documents = claimDocumentService.getDocuments(claimId);
        documents.forEach(document -> claimDocumentService.deleteDocument(document.getDocumentId()));
        claimRepository.delete(claim);
        log.info("Claim deleted claimId={}", claimId);
    }

    private void validateCoverageAndRelationships(
            ClaimDTO dto,
            Patient patient,
            Invoice invoice,
            InsuranceCompany company,
            PatientInsurance patientInsurance) {

        if (dto.getDateOfService() == null) {
            throw new BusinessValidationException("dateOfService", "Date of service is required.");
        }
        validateMoneyAmount(dto.getClaimAmount(), "claimAmount", "Claim amount");

        if (invoice.getPatient() == null
                || invoice.getPatient().getPatientId() != patient.getPatientId()) {
            throw new BusinessValidationException(
                    "invoiceId",
                    "The selected invoice does not belong to the selected patient.");
        }

        String invoiceStatus = invoice.getStatus() == null
                ? ""
                : invoice.getStatus().trim().toUpperCase();
        if (!CLAIMABLE_INVOICE_STATUSES.contains(invoiceStatus)) {
            throw new BusinessValidationException(
                    "invoiceId",
                    "Only pending, unpaid, or overdue invoices can be claimed. Paid invoices do not need a claim.");
        }
        if (invoicePaymentRepository.existsByInvoiceInvoiceId(invoice.getInvoiceId())) {
            throw new BusinessValidationException(
                    "invoiceId",
                    "This invoice has already been paid and cannot be submitted for insurance.");
        }

        if (patientInsurance.getPatient() == null
                || patientInsurance.getPatient().getPatientId() != patient.getPatientId()) {
            throw new BusinessValidationException(
                    "enrollmentId",
                    "The selected insurance policy does not belong to the patient.");
        }

        if (patientInsurance.getInsurancePlan() == null
                || patientInsurance.getInsurancePlan().getInsuranceCompany() == null) {
            throw new BusinessValidationException(
                    "enrollmentId",
                    "The selected insurance policy is not linked to an insurance company.");
        }

        int policyCompanyId = patientInsurance.getInsurancePlan().getInsuranceCompany().getCompanyId();
        if (policyCompanyId != company.getCompanyId()) {
            throw new BusinessValidationException(
                    "enrollmentId",
                    "The selected insurance company does not provide the selected policy.");
        }

        if (!"ACTIVE".equalsIgnoreCase(patientInsurance.getStatus())) {
            throw new BusinessValidationException("enrollmentId", "The selected insurance policy is not active.");
        }

        if (!patientInsurance.getInsurancePlan().isActive()) {
            throw new BusinessValidationException("enrollmentId", "The selected insurance plan is inactive.");
        }

        LocalDate today = LocalDate.now();
        if (patientInsurance.getEnrollmentDate() == null
                || patientInsurance.getExpiryDate() == null
                || today.isBefore(patientInsurance.getEnrollmentDate())
                || today.isAfter(patientInsurance.getExpiryDate())) {
            throw new BusinessValidationException(
                    "enrollmentId",
                    "The selected insurance policy is outside its active date range.");
        }

        if (dto.getDateOfService().isBefore(patientInsurance.getEnrollmentDate())
                || dto.getDateOfService().isAfter(patientInsurance.getExpiryDate())) {
            throw new BusinessValidationException(
                    "dateOfService",
                    "Date of service must be within the insurance policy period.");
        }
        if (invoice.getInvoiceDate() != null
                && dto.getDateOfService().isAfter(invoice.getInvoiceDate())) {
            throw new BusinessValidationException(
                    "dateOfService",
                    "Date of service cannot be after the invoice date.");
        }

        if (invoice.getTotalAmount() == null || invoice.getTotalAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessValidationException("invoiceId", "The selected invoice has no valid total amount.");
        }

        if (dto.getClaimAmount().compareTo(invoice.getTotalAmount()) > 0) {
            throw new BusinessValidationException(
                    "claimAmount",
                    "Claim amount cannot exceed the invoice total of " + invoice.getTotalAmount() + ".");
        }

        BigDecimal coverageAmount = patientInsurance.getInsurancePlan().getCoverageAmount();
        if (coverageAmount == null || coverageAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessValidationException("enrollmentId", "The selected plan has no valid coverage amount.");
        }

        BigDecimal approvedCoverageUsed = safeAmount(
                claimRepository.sumApprovedAmountByEnrollmentId(patientInsurance.getEnrollmentId()));
        BigDecimal remainingCoverage = coverageAmount.subtract(approvedCoverageUsed).max(BigDecimal.ZERO);
        if (remainingCoverage.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessValidationException(
                    "enrollmentId",
                    "The selected policy has no remaining coverage.");
        }

        if (claimRepository.existsByInvoiceInvoiceIdAndStatusIn(invoice.getInvoiceId(), ACTIVE_CLAIM_STATUSES)) {
            throw new BusinessValidationException(
                    "invoiceId",
                    "A pending or approved claim already exists for this invoice.");
        }
    }

    private void validateApprovedAmount(Claim claim, BigDecimal approvedAmount) {
        validateMoneyAmount(approvedAmount, "approvedAmount", "Approved amount");
        if (approvedAmount.compareTo(claim.getClaimAmount()) > 0) {
            throw new BusinessValidationException(
                    "approvedAmount",
                    "Approved amount cannot exceed the requested claim amount of " + claim.getClaimAmount() + ".");
        }
        BigDecimal invoiceTotal = safeAmount(claim.getInvoice().getTotalAmount());
        if (approvedAmount.compareTo(invoiceTotal) > 0) {
            throw new BusinessValidationException(
                    "approvedAmount",
                    "Approved amount cannot exceed the invoice total of " + invoiceTotal + ".");
        }
    }

    private void lockPolicyAndValidateRemainingCoverage(Claim claim, BigDecimal approvedAmount) {
        PatientInsurance currentInsurance = claim.getPatientInsurance();
        if (currentInsurance == null) {
            throw new BusinessValidationException(
                    "enrollmentId",
                    "This claim is not linked to a valid insurance policy.");
        }

        PatientInsurance lockedInsurance = patientInsuranceRepository
                .findByIdForUpdate(currentInsurance.getEnrollmentId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Insurance enrollment not found with id: " + currentInsurance.getEnrollmentId()));
        if (lockedInsurance.getInsurancePlan() == null) {
            throw new BusinessValidationException(
                    "enrollmentId",
                    "This claim is not linked to a valid insurance plan.");
        }

        BigDecimal coverage = safeAmount(lockedInsurance.getInsurancePlan().getCoverageAmount());
        BigDecimal approved = safeAmount(
                claimRepository.sumApprovedAmountByEnrollmentId(lockedInsurance.getEnrollmentId()));
        BigDecimal remaining = coverage.subtract(approved).max(BigDecimal.ZERO);

        if (approvedAmount.compareTo(remaining) > 0) {
            throw new BusinessValidationException(
                    "approvedAmount",
                    "Approved amount exceeds the remaining policy coverage of " + remaining + ".");
        }
    }


    private void validateInvoiceCanReceiveInsurancePayment(Invoice invoice) {
        if (invoice == null) {
            throw new BusinessValidationException("invoiceId", "The claim is not linked to a valid invoice.");
        }
        String status = invoice.getStatus() == null ? "" : invoice.getStatus().trim().toUpperCase();
        if ("PAID".equals(status) || invoicePaymentRepository.existsByInvoiceInvoiceId(invoice.getInvoiceId())) {
            throw new BusinessValidationException(
                    "invoiceId",
                    "This invoice has already been paid and the claim can no longer be approved.");
        }
        if ("CANCELLED".equals(status)) {
            throw new BusinessValidationException(
                    "invoiceId",
                    "A claim linked to a cancelled invoice cannot be approved.");
        }
    }

    private void validateMoneyAmount(BigDecimal amount, String field, String label) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessValidationException(field, label + " must be greater than 0.");
        }
        if (amount.compareTo(MAX_MONEY_AMOUNT) > 0) {
            throw new BusinessValidationException(field, label + " cannot exceed " + MAX_MONEY_AMOUNT + ".");
        }
        if (Math.max(0, amount.stripTrailingZeros().scale()) > 2) {
            throw new BusinessValidationException(field, label + " can have at most 2 decimal places.");
        }
    }

    private void validateInsuranceCompanyAccess(InsuranceCompany company) {
        Authentication authentication = currentAuthentication();
        if (authentication == null) {
            return;
        }
        Set<String> roles = authentication.getAuthorities().stream()
                .map(authority -> authority.getAuthority())
                .collect(java.util.stream.Collectors.toSet());
        if (roles.contains("ROLE_ADMIN")) {
            return;
        }
        boolean owner = roles.contains("ROLE_INSURANCE")
                && company.getAppUser() != null
                && authentication.getName().equalsIgnoreCase(company.getAppUser().getEmail());
        if (!owner) {
            throw new AccessDeniedException("Insurance companies can view only their own active claims.");
        }
    }

    private void validateReadAccess(Claim claim) {
        if (!canCurrentUserRead(claim)) {
            throw new AccessDeniedException("You are not allowed to view this claim.");
        }
    }

    private boolean canCurrentUserRead(Claim claim) {
        Authentication authentication = currentAuthentication();
        if (authentication == null) {
            return true;
        }

        Set<String> roles = authentication.getAuthorities().stream()
                .map(authority -> authority.getAuthority())
                .collect(java.util.stream.Collectors.toSet());
        if (roles.contains("ROLE_ADMIN")) {
            return true;
        }

        String email = authentication.getName();
        boolean patientOwner = claim.getPatient() != null
                && claim.getPatient().getAppUser() != null
                && email.equalsIgnoreCase(claim.getPatient().getAppUser().getEmail());
        boolean providerOwner = claim.getInvoice() != null
                && claim.getInvoice().getHealthcareProvider() != null
                && claim.getInvoice().getHealthcareProvider().getAppUser() != null
                && email.equalsIgnoreCase(
                        claim.getInvoice().getHealthcareProvider().getAppUser().getEmail());
        boolean insuranceOwner = claim.getInsuranceCompany() != null
                && claim.getInsuranceCompany().getAppUser() != null
                && email.equalsIgnoreCase(claim.getInsuranceCompany().getAppUser().getEmail());

        return patientOwner || providerOwner || insuranceOwner;
    }

    private void validateSubmitterOwnership(Patient patient, Invoice invoice) {
        Authentication authentication = currentAuthentication();
        if (authentication == null) {
            return;
        }

        Set<String> roles = authentication.getAuthorities().stream()
                .map(authority -> authority.getAuthority())
                .collect(java.util.stream.Collectors.toSet());
        String email = authentication.getName();

        if (roles.contains("ROLE_ADMIN")) {
            return;
        }
        if (roles.contains("ROLE_PATIENT")) {
            if (patient.getAppUser() == null
                    || !email.equalsIgnoreCase(patient.getAppUser().getEmail())) {
                throw new AccessDeniedException("Patients can submit claims only for their own profile.");
            }
            return;
        }
        if (roles.contains("ROLE_PROVIDER")) {
            if (invoice.getHealthcareProvider() == null
                    || invoice.getHealthcareProvider().getAppUser() == null
                    || !email.equalsIgnoreCase(invoice.getHealthcareProvider().getAppUser().getEmail())) {
                throw new AccessDeniedException("Providers can submit claims only for invoices they generated.");
            }
            return;
        }
        throw new AccessDeniedException("You are not allowed to submit claims.");
    }

    private void validateInsuranceDecisionOwner(Claim claim) {
        Authentication authentication = currentAuthentication();
        if (authentication == null) {
            return;
        }

        Set<String> roles = authentication.getAuthorities().stream()
                .map(authority -> authority.getAuthority())
                .collect(java.util.stream.Collectors.toSet());
        if (roles.contains("ROLE_ADMIN")) {
            return;
        }

        boolean ownsClaim = roles.contains("ROLE_INSURANCE")
                && claim.getInsuranceCompany() != null
                && claim.getInsuranceCompany().getAppUser() != null
                && authentication.getName().equalsIgnoreCase(
                        claim.getInsuranceCompany().getAppUser().getEmail());
        if (!ownsClaim) {
            throw new AccessDeniedException("Insurance companies can process only their own claims.");
        }
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

    private Claim getClaimEntity(Integer claimId) {
        return claimRepository.findById(claimId)
                .orElseThrow(() -> new ResourceNotFoundException("Claim not found with id: " + claimId));
    }

    private Patient getPatient(Integer patientId) {
        return patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + patientId));
    }

    private Invoice getInvoice(Integer invoiceId) {
        return invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with id: " + invoiceId));
    }

    private InsuranceCompany getCompany(Integer companyId) {
        return companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Insurance company not found with id: " + companyId));
    }

    private PatientInsurance getPatientInsurance(Integer enrollmentId) {
        return patientInsuranceRepository.findById(enrollmentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Insurance enrollment not found with id: " + enrollmentId));
    }

    private void sendClaimSubmittedEmails(Claim claim) {
        try {
            String patientEmail = claim.getPatient().getAppUser().getEmail();
            mailService.sendSimpleEmail(
                    patientEmail,
                    "CareAssist claim submitted",
                    "Your claim " + claim.getClaimId() + " was submitted successfully and is pending review.",
                    "CareAssist Claims",
                    getInsuranceReplyToEmail(claim));

            String insuranceEmail = getInsuranceReplyToEmail(claim);
            if (insuranceEmail != null && !insuranceEmail.equalsIgnoreCase(patientEmail)) {
                mailService.sendSimpleEmail(
                        insuranceEmail,
                        "New CareAssist claim " + claim.getClaimId(),
                        "A new claim was submitted by " + claim.getPatient().getFullName()
                                + ". Claim amount: " + claim.getClaimAmount() + ".",
                        "CareAssist - " + claim.getPatient().getFullName(),
                        patientEmail);
            }
        } catch (Exception exception) {
            log.error("Unable to send claim-submission notification claimId={}", claim.getClaimId(), exception);
        }
    }

    private void sendClaimStatusEmail(Claim claim, String statusText, String message) {
        try {
            String patientEmail = claim.getPatient().getAppUser().getEmail();
            String companyName = getInsuranceDisplayName(claim);
            String companyReplyTo = getInsuranceReplyToEmail(claim);

            mailService.sendSimpleEmail(
                    patientEmail,
                    "CareAssist claim " + statusText,
                    message + System.lineSeparator() + "Claim ID: " + claim.getClaimId(),
                    "CareAssist - " + companyName,
                    companyReplyTo);

            log.info(
                    "Claim status email sent claimId={} status={} patientEmail={} replyTo={}",
                    claim.getClaimId(),
                    statusText,
                    patientEmail,
                    companyReplyTo);
        } catch (Exception exception) {
            log.error(
                    "Unable to send claim status email claimId={} status={}",
                    claim.getClaimId(),
                    statusText,
                    exception);
        }
    }

    private String getInsuranceDisplayName(Claim claim) {
        if (claim.getInsuranceCompany() != null
                && claim.getInsuranceCompany().getCompanyName() != null
                && !claim.getInsuranceCompany().getCompanyName().isBlank()) {
            return claim.getInsuranceCompany().getCompanyName();
        }
        return "Insurance Company";
    }

    private String getInsuranceReplyToEmail(Claim claim) {
        if (claim.getInsuranceCompany() == null) {
            return null;
        }
        if (claim.getInsuranceCompany().getContactEmail() != null
                && !claim.getInsuranceCompany().getContactEmail().isBlank()) {
            return claim.getInsuranceCompany().getContactEmail();
        }
        if (claim.getInsuranceCompany().getAppUser() != null
                && claim.getInsuranceCompany().getAppUser().getEmail() != null
                && !claim.getInsuranceCompany().getAppUser().getEmail().isBlank()) {
            return claim.getInsuranceCompany().getAppUser().getEmail();
        }
        return null;
    }

    private ClaimDTO claimToDTO(Claim claim) {
        ClaimDTO dto = new ClaimDTO();
        dto.setClaimId(claim.getClaimId());
        dto.setPatientId(claim.getPatient().getPatientId());
        dto.setInvoiceId(claim.getInvoice().getInvoiceId());
        dto.setCompanyId(claim.getInsuranceCompany().getCompanyId());
        dto.setEnrollmentId(claim.getPatientInsurance() == null
                ? null
                : claim.getPatientInsurance().getEnrollmentId());
        dto.setDiagnosis(claim.getDiagnosis());
        dto.setTreatment(claim.getTreatment());
        dto.setDateOfService(claim.getDateOfService());
        dto.setClaimAmount(claim.getClaimAmount());
        dto.setApprovedAmount(claim.getApprovedAmount());
        dto.setSubmissionDate(claim.getSubmissionDate());
        dto.setApprovalDate(claim.getApprovalDate());
        dto.setStatus(claim.getStatus());
        dto.setRejectionReason(claim.getRejectionReason());

        dto.setPatientName(claim.getPatient().getFullName());
        if (claim.getPatient().getAppUser() != null) {
            dto.setPatientEmail(claim.getPatient().getAppUser().getEmail());
        }
        dto.setInvoiceNumber(claim.getInvoice().getInvoiceNumber());
        dto.setInvoiceAmount(claim.getInvoice().getTotalAmount());
        if (claim.getInvoice().getHealthcareProvider() != null) {
            dto.setProviderId(claim.getInvoice().getHealthcareProvider().getProviderId());
            dto.setProviderName(claim.getInvoice().getHealthcareProvider().getProviderName());
        }
        dto.setCompanyName(claim.getInsuranceCompany().getCompanyName());

        claimPaymentRepository.findByClaimClaimId(claim.getClaimId()).ifPresent(payment -> {
            dto.setInsurancePaymentId(payment.getPaymentId());
            dto.setInsurancePaymentDate(payment.getPaymentDate());
            dto.setInsurancePaidAmount(payment.getPaymentAmount());
            dto.setInsurancePaymentMode(payment.getPaymentMode());
            dto.setInsuranceTransactionReference(payment.getTransactionReference());
            dto.setInsurancePaymentProcessed(true);
        });

        if (claim.getPatientInsurance() != null
                && claim.getPatientInsurance().getInsurancePlan() != null) {
            dto.setPlanId(claim.getPatientInsurance().getInsurancePlan().getPlanId());
            dto.setPlanName(claim.getPatientInsurance().getInsurancePlan().getPlanName());
            BigDecimal coverage = safeAmount(
                    claim.getPatientInsurance().getInsurancePlan().getCoverageAmount());
            BigDecimal approved = safeAmount(
                    claimRepository.sumApprovedAmountByEnrollmentId(
                            claim.getPatientInsurance().getEnrollmentId()));
            BigDecimal remaining = coverage.subtract(approved).max(BigDecimal.ZERO);
            dto.setCoverageAmount(coverage);
            dto.setApprovedCoverageUsed(approved);
            dto.setRemainingCoverage(remaining);

            if ("PENDING".equalsIgnoreCase(claim.getStatus())) {
                dto.setMaxApprovableAmount(minAmount(
                        safeAmount(claim.getClaimAmount()),
                        safeAmount(claim.getInvoice().getTotalAmount()),
                        remaining));
            } else if ("APPROVED".equalsIgnoreCase(claim.getStatus())) {
                dto.setMaxApprovableAmount(effectiveApprovedAmount(claim));
            } else {
                dto.setMaxApprovableAmount(BigDecimal.ZERO);
            }
        }

        dto.setDocumentCount(claimDocumentRepository.countByClaimClaimId(claim.getClaimId()));
        return dto;
    }

    private BigDecimal effectiveApprovedAmount(Claim claim) {
        if (claim.getApprovedAmount() != null) {
            return claim.getApprovedAmount();
        }
        return "APPROVED".equalsIgnoreCase(claim.getStatus())
                ? safeAmount(claim.getClaimAmount())
                : BigDecimal.ZERO;
    }

    private BigDecimal minAmount(BigDecimal... values) {
        BigDecimal result = null;
        for (BigDecimal value : values) {
            BigDecimal safe = safeAmount(value);
            result = result == null || safe.compareTo(result) < 0 ? safe : result;
        }
        return result == null ? BigDecimal.ZERO : result.max(BigDecimal.ZERO);
    }

    private BigDecimal safeAmount(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
