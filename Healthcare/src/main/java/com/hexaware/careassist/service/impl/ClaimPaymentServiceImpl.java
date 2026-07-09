package com.hexaware.careassist.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
//import java.util.stream.Collectors;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hexaware.careassist.dto.ClaimPaymentDTO;
import com.hexaware.careassist.entity.Claim;
import com.hexaware.careassist.entity.ClaimPayment;
import com.hexaware.careassist.entity.Invoice;
import com.hexaware.careassist.exception.BusinessValidationException;
import com.hexaware.careassist.exception.ResourceNotFoundException;
import com.hexaware.careassist.repository.ClaimPaymentRepository;
import com.hexaware.careassist.repository.ClaimRepository;
import com.hexaware.careassist.repository.InvoicePaymentRepository;
import com.hexaware.careassist.repository.InvoiceRepository;
import com.hexaware.careassist.service.IClaimPaymentService;
import com.hexaware.careassist.service.IMailService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ClaimPaymentServiceImpl implements IClaimPaymentService {

    private static final Set<String> ALLOWED_PAYMENT_MODES =
            Set.of("CASH", "CARD", "UPI", "NET_BANKING", "CHEQUE");
    private static final Pattern TRANSACTION_REFERENCE_PATTERN =
            Pattern.compile("^[A-Z0-9][A-Z0-9/_-]{5,59}$");

    private final ClaimPaymentRepository paymentRepository;
    private final ClaimRepository claimRepository;
    private final InvoiceRepository invoiceRepository;
    private final InvoicePaymentRepository invoicePaymentRepository;
    private final IMailService mailService;

    @Override
    public ClaimPaymentDTO processClaimPayment(ClaimPaymentDTO dto) {
        Claim claim = getClaim(dto.getClaimId());
        validateInsuranceOwner(claim);

        if (!"APPROVED".equalsIgnoreCase(claim.getStatus())) {
            throw new BusinessValidationException(
                    "claimId",
                    "Payment can be processed only for an approved claim.");
        }
        if (paymentRepository.existsByClaimClaimId(claim.getClaimId())) {
            throw new BusinessValidationException(
                    "claimId",
                    "Insurance payment has already been processed for this claim.");
        }

        BigDecimal approvedAmount = effectiveApprovedAmount(claim);
        if (approvedAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessValidationException(
                    "claimId",
                    "The approved claim amount is invalid.");
        }

        Invoice invoice = claim.getInvoice();
        validateInvoiceCanReceiveInsurancePayment(invoice);
        BigDecimal invoiceTotal = safeAmount(invoice.getTotalAmount());
        if (approvedAmount.compareTo(invoiceTotal) > 0) {
            throw new BusinessValidationException(
                    "claimId",
                    "Approved amount cannot exceed the invoice total.");
        }

        String paymentMode = normalize(dto.getPaymentMode());
        if (!ALLOWED_PAYMENT_MODES.contains(paymentMode)) {
            throw new BusinessValidationException(
                    "paymentMode",
                    "Payment mode must be CASH, CARD, UPI, NET_BANKING, or CHEQUE.");
        }

        String transactionReference = normalizeReference(dto.getTransactionReference());
        if (transactionReference.isBlank()) {
            transactionReference = generateReference(claim.getClaimId());
        } else if (!TRANSACTION_REFERENCE_PATTERN.matcher(transactionReference).matches()) {
            throw new BusinessValidationException(
                    "transactionReference",
                    "Transaction reference must be 6-60 characters using only letters, numbers, slash, underscore, or hyphen.");
        }
        if (paymentRepository.existsByTransactionReferenceIgnoreCase(transactionReference)) {
            throw new BusinessValidationException(
                    "transactionReference",
                    "Transaction reference already exists.");
        }

        ClaimPayment payment = new ClaimPayment();
        payment.setClaim(claim);
        payment.setPaymentDate(LocalDateTime.now());
        payment.setPaymentAmount(approvedAmount);
        payment.setPaymentMode(paymentMode);
        payment.setTransactionReference(transactionReference);

        ClaimPayment savedPayment = paymentRepository.save(payment);

        BigDecimal remainingAmount = invoiceTotal.subtract(approvedAmount).max(BigDecimal.ZERO);
        if (remainingAmount.compareTo(BigDecimal.ZERO) == 0) {
            invoice.setStatus("PAID");
            invoiceRepository.save(invoice);
        }

        sendClaimPaymentEmail(savedPayment, remainingAmount);
        log.info(
                "Insurance payment processed paymentId={} claimId={} approvedAmount={} remainingInvoiceAmount={}",
                savedPayment.getPaymentId(),
                claim.getClaimId(),
                approvedAmount,
                remainingAmount);
        return paymentToDTO(savedPayment);
    }

    @Override
    @Transactional(readOnly = true)
    public ClaimPaymentDTO getPaymentById(Integer paymentId) {
        ClaimPayment payment = getPaymentEntity(paymentId);
        validateInsuranceOwner(payment.getClaim());
        return paymentToDTO(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public ClaimPaymentDTO getPaymentByClaimId(Integer claimId) {
        ClaimPayment payment = paymentRepository.findByClaimClaimId(claimId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Payment not found for claim id: " + claimId));
        validateInsuranceOwner(payment.getClaim());
        return paymentToDTO(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClaimPaymentDTO> getAllPayments() {
        Authentication authentication = currentAuthentication();
        if (authentication == null || hasRole(authentication, "ROLE_ADMIN")) {
            return paymentRepository.findAll().stream()
                    .map(this::paymentToDTO)
                    .toList();
        }

        if (!hasRole(authentication, "ROLE_INSURANCE")) {
            throw new AccessDeniedException("Only insurance companies or administrators can view claim payments.");
        }

        return paymentRepository.findAll().stream()
                .filter(payment -> ownsClaim(authentication, payment.getClaim()))
                .map(this::paymentToDTO)
                .toList();
    }

    private void validateInvoiceCanReceiveInsurancePayment(Invoice invoice) {
        if (invoice == null) {
            throw new BusinessValidationException("invoiceId", "The claim is not linked to a valid invoice.");
        }
        String invoiceStatus = normalize(invoice.getStatus());
        if ("PAID".equals(invoiceStatus)
                || invoicePaymentRepository.existsByInvoiceInvoiceId(invoice.getInvoiceId())) {
            throw new BusinessValidationException(
                    "invoiceId",
                    "This invoice has already been paid and cannot receive an insurance payment.");
        }
        if ("CANCELLED".equals(invoiceStatus)) {
            throw new BusinessValidationException(
                    "invoiceId",
                    "A cancelled invoice cannot receive an insurance payment.");
        }
        if (safeAmount(invoice.getTotalAmount()).compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessValidationException(
                    "invoiceId",
                    "The linked invoice has no valid payable amount.");
        }
    }

    private ClaimPayment getPaymentEntity(Integer paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Claim payment not found with id: " + paymentId));
    }

    private Claim getClaim(Integer claimId) {
        return claimRepository.findById(claimId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Claim not found with id: " + claimId));
    }

    private void validateInsuranceOwner(Claim claim) {
        Authentication authentication = currentAuthentication();
        if (authentication == null || hasRole(authentication, "ROLE_ADMIN")) {
            return;
        }
        if (!hasRole(authentication, "ROLE_INSURANCE") || !ownsClaim(authentication, claim)) {
            throw new AccessDeniedException("Insurance companies can process only their own claims.");
        }
    }

    private boolean ownsClaim(Authentication authentication, Claim claim) {
        return claim != null
                && claim.getInsuranceCompany() != null
                && claim.getInsuranceCompany().getAppUser() != null
                && authentication.getName().equalsIgnoreCase(
                        claim.getInsuranceCompany().getAppUser().getEmail());
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

    private BigDecimal effectiveApprovedAmount(Claim claim) {
        return claim.getApprovedAmount() == null
                ? safeAmount(claim.getClaimAmount())
                : claim.getApprovedAmount();
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeReference(String value) {
        return value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
    }

    private String generateReference(Integer claimId) {
        String random = UUID.randomUUID().toString().replace("-", "")
                .substring(0, 12).toUpperCase(Locale.ROOT);
        return "CLM-" + claimId + "-" + random;
    }

    private BigDecimal safeAmount(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private void sendClaimPaymentEmail(ClaimPayment payment, BigDecimal remainingAmount) {
        try {
            Claim claim = payment.getClaim();
            String patientEmail = claim.getPatient().getAppUser().getEmail();
            String companyName = getInsuranceDisplayName(claim);
            String companyReplyTo = getInsuranceReplyToEmail(claim);

            String settlementMessage = remainingAmount.compareTo(BigDecimal.ZERO) == 0
                    ? "The invoice is now fully paid by insurance."
                    : "Patient remaining invoice balance: " + remainingAmount;

            mailService.sendSimpleEmail(
                    patientEmail,
                    "CareAssist insurance payment processed",
                    "Your insurance payment has been processed." + System.lineSeparator()
                            + "Claim ID: " + claim.getClaimId() + System.lineSeparator()
                            + "Insurance paid: " + payment.getPaymentAmount() + System.lineSeparator()
                            + settlementMessage + System.lineSeparator()
                            + "Transaction reference: " + payment.getTransactionReference(),
                    "CareAssist - " + companyName,
                    companyReplyTo);
        } catch (Exception exception) {
            log.error("Unable to send claim payment email paymentId={}", payment.getPaymentId(), exception);
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

    private ClaimPaymentDTO paymentToDTO(ClaimPayment payment) {
        Claim claim = payment.getClaim();
        Invoice invoice = claim.getInvoice();

        ClaimPaymentDTO dto = new ClaimPaymentDTO();
        dto.setPaymentId(payment.getPaymentId());
        dto.setClaimId(claim.getClaimId());
        dto.setInvoiceId(invoice.getInvoiceId());
        dto.setInvoiceNumber(invoice.getInvoiceNumber());
        dto.setPatientId(claim.getPatient().getPatientId());
        dto.setPatientName(claim.getPatient().getFullName());
        dto.setCompanyId(claim.getInsuranceCompany().getCompanyId());
        dto.setCompanyName(claim.getInsuranceCompany().getCompanyName());
        dto.setApprovedAmount(effectiveApprovedAmount(claim));
        dto.setPaymentDate(payment.getPaymentDate());
        dto.setPaymentAmount(payment.getPaymentAmount());
        dto.setPaymentMode(payment.getPaymentMode());
        dto.setTransactionReference(payment.getTransactionReference());
        return dto;
    }
}
