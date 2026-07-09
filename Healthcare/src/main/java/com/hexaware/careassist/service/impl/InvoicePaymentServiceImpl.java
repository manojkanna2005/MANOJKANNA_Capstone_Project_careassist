package com.hexaware.careassist.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hexaware.careassist.dto.InvoicePaymentDTO;
import com.hexaware.careassist.dto.InvoicePaymentRequest;
import com.hexaware.careassist.entity.Claim;
import com.hexaware.careassist.entity.ClaimPayment;
import com.hexaware.careassist.entity.Invoice;
import com.hexaware.careassist.entity.InvoicePayment;
import com.hexaware.careassist.entity.Patient;
import com.hexaware.careassist.exception.BusinessValidationException;
import com.hexaware.careassist.exception.ResourceNotFoundException;
import com.hexaware.careassist.repository.ClaimPaymentRepository;
import com.hexaware.careassist.repository.ClaimRepository;
import com.hexaware.careassist.repository.InvoicePaymentRepository;
import com.hexaware.careassist.repository.InvoiceRepository;
import com.hexaware.careassist.repository.PatientRepository;
import com.hexaware.careassist.service.IInvoicePaymentService;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class InvoicePaymentServiceImpl implements IInvoicePaymentService {

    private static final Set<String> ALLOWED_PAYMENT_METHODS =
            Set.of("CARD", "UPI", "NET_BANKING", "CASH");

    private final InvoicePaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;
    private final ClaimRepository claimRepository;
    private final ClaimPaymentRepository claimPaymentRepository;
    private final PatientRepository patientRepository;

    @Override
    public InvoicePaymentDTO processPayment(InvoicePaymentRequest request, String authenticatedEmail) {
        Patient patient = getPatientByEmail(authenticatedEmail);
        Invoice invoice = invoiceRepository.findById(request.getInvoiceId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Invoice not found with id: " + request.getInvoiceId()));

        verifyOwnership(invoice, patient);

        String invoiceStatus = normalize(invoice.getStatus());
        if (paymentRepository.existsByInvoiceInvoiceId(invoice.getInvoiceId())
                || "PAID".equals(invoiceStatus)) {
            throw new BusinessValidationException("This invoice has already been paid.");
        }
        if ("CANCELLED".equals(invoiceStatus)) {
            throw new BusinessValidationException("A cancelled invoice cannot be paid.");
        }
        if (!isPayableInvoiceStatus(invoiceStatus)) {
            throw new BusinessValidationException(
                    "This invoice cannot be paid in its current status.");
        }

        Optional<Claim> latestClaimOptional = claimRepository
                .findFirstByInvoiceInvoiceIdOrderByClaimIdDesc(invoice.getInvoiceId());

        Claim paymentClaim = null;
        BigDecimal insurancePaid = BigDecimal.ZERO;

        if (latestClaimOptional.isPresent()) {
            Claim latestClaim = latestClaimOptional.get();
            String claimStatus = normalize(latestClaim.getStatus());

            if (isClaimInProgress(claimStatus)) {
                throw new BusinessValidationException(
                        "An insurance claim is currently in progress. Wait for the insurance decision before paying this invoice.");
            }

            if ("APPROVED".equals(claimStatus)) {
                ClaimPayment insurancePayment = claimPaymentRepository
                        .findByClaimClaimId(latestClaim.getClaimId())
                        .orElseThrow(() -> new BusinessValidationException(
                                "Wait until the insurance company processes the approved claim amount."));

                BigDecimal approvedAmount = latestClaim.getApprovedAmount() == null
                        ? safeAmount(latestClaim.getClaimAmount())
                        : safeAmount(latestClaim.getApprovedAmount());
                insurancePaid = safeAmount(insurancePayment.getPaymentAmount());

                if (insurancePaid.compareTo(approvedAmount) != 0) {
                    throw new BusinessValidationException(
                            "The recorded insurance payment does not match the approved claim amount.");
                }
                paymentClaim = latestClaim;
            } else if (!isClosedWithoutInsurancePayment(claimStatus)) {
                throw new BusinessValidationException(
                        "This invoice cannot be paid while the claim status is " + claimStatus + ".");
            }
        }

        BigDecimal invoiceTotal = safeAmount(invoice.getTotalAmount());
        BigDecimal remainingAmount = invoiceTotal.subtract(insurancePaid).max(BigDecimal.ZERO);

        if (remainingAmount.compareTo(BigDecimal.ZERO) <= 0) {
            invoice.setStatus("PAID");
            invoiceRepository.save(invoice);
            throw new BusinessValidationException(
                    "This invoice has already been fully settled by insurance.");
        }

        String paymentMethod = normalize(request.getPaymentMethod());
        if (!ALLOWED_PAYMENT_METHODS.contains(paymentMethod)) {
            throw new BusinessValidationException(
                    "paymentMethod",
                    "Payment method must be CARD, UPI, NET_BANKING, or CASH.");
        }

        InvoicePayment payment = new InvoicePayment();
        payment.setInvoice(invoice);
        payment.setClaim(paymentClaim);
        payment.setPaymentDate(LocalDateTime.now());
        payment.setPaymentAmount(remainingAmount);
        payment.setPaymentMethod(paymentMethod);
        payment.setTransactionReference(generateReference(invoice.getInvoiceId()));
        payment.setPaymentStatus("SUCCESS");

        InvoicePayment saved = paymentRepository.save(payment);
        invoice.setStatus("PAID");
        invoiceRepository.save(invoice);

        return toDTO(saved, insurancePaid);
    }

    @Override
    @Transactional(readOnly = true)
    public InvoicePaymentDTO getPaymentByInvoiceId(
            Integer invoiceId,
            String authenticatedEmail,
            boolean admin) {
        InvoicePayment payment = paymentRepository.findByInvoiceInvoiceId(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Payment not found for invoice id: " + invoiceId));

        if (!admin) {
            Patient patient = getPatientByEmail(authenticatedEmail);
            verifyOwnership(payment.getInvoice(), patient);
        }

        return toDTO(payment, getInsurancePaidAmount(payment));
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvoicePaymentDTO> getMyPayments(String authenticatedEmail) {
        Patient patient = getPatientByEmail(authenticatedEmail);
        return paymentRepository
                .findByInvoicePatientPatientIdOrderByPaymentDateDesc(patient.getPatientId())
                .stream()
                .map(payment -> toDTO(payment, getInsurancePaidAmount(payment)))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvoicePaymentDTO> getAllPayments() {
        return paymentRepository.findAll().stream()
                .map(payment -> toDTO(payment, getInsurancePaidAmount(payment)))
                .toList();
    }

    private BigDecimal getInsurancePaidAmount(InvoicePayment payment) {
        if (payment.getClaim() == null) {
            return BigDecimal.ZERO;
        }
        return claimPaymentRepository.findByClaimClaimId(payment.getClaim().getClaimId())
                .map(ClaimPayment::getPaymentAmount)
                .map(this::safeAmount)
                .orElse(BigDecimal.ZERO);
    }

    private Patient getPatientByEmail(String email) {
        return patientRepository.findTopByAppUserEmailIgnoreCaseOrderByPatientIdDesc(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Patient profile was not found for the logged-in account."));
    }

    private void verifyOwnership(Invoice invoice, Patient patient) {
        if (invoice.getPatient() == null
                || invoice.getPatient().getPatientId() != patient.getPatientId()) {
            throw new AccessDeniedException("You are not allowed to access this invoice.");
        }
    }

    private boolean isPayableInvoiceStatus(String status) {
        return "PENDING".equals(status)
                || "UNPAID".equals(status)
                || "OVERDUE".equals(status);
    }

    private boolean isClaimInProgress(String status) {
        return "PENDING".equals(status)
                || "SUBMITTED".equals(status)
                || "UNDER_REVIEW".equals(status);
    }

    private boolean isClosedWithoutInsurancePayment(String status) {
        return "REJECTED".equals(status)
                || "DENIED".equals(status)
                || "CANCELLED".equals(status)
                || "WITHDRAWN".equals(status);
    }

    private String generateReference(Integer invoiceId) {
        String random = UUID.randomUUID().toString().replace("-", "")
                .substring(0, 12).toUpperCase(Locale.ROOT);
        return "INV-" + invoiceId + "-" + random;
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
    }

    private BigDecimal safeAmount(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private InvoicePaymentDTO toDTO(InvoicePayment payment, BigDecimal insurancePaidAmount) {
        Invoice invoice = payment.getInvoice();
        Claim claim = payment.getClaim();
        BigDecimal invoiceTotal = safeAmount(invoice.getTotalAmount());
        BigDecimal insurancePaid = safeAmount(insurancePaidAmount);
        BigDecimal patientPaid = safeAmount(payment.getPaymentAmount());

        InvoicePaymentDTO dto = new InvoicePaymentDTO();
        dto.setPaymentId(payment.getPaymentId());
        dto.setInvoiceId(invoice.getInvoiceId());
        dto.setInvoiceNumber(invoice.getInvoiceNumber());
        dto.setPatientId(invoice.getPatient().getPatientId());
        dto.setPatientName(invoice.getPatient().getFullName());
        dto.setProviderId(invoice.getHealthcareProvider().getProviderId());
        dto.setProviderName(invoice.getHealthcareProvider().getProviderName());
        if (claim != null) {
            dto.setClaimId(claim.getClaimId());
            dto.setClaimStatus(claim.getStatus());
        }
        dto.setInvoiceTotal(invoiceTotal);
        dto.setInsurancePaidAmount(insurancePaid);
        dto.setPaymentDate(payment.getPaymentDate());
        dto.setPaymentAmount(patientPaid);
        dto.setRemainingAmount(
                invoiceTotal.subtract(insurancePaid).subtract(patientPaid).max(BigDecimal.ZERO));
        dto.setPaymentMethod(payment.getPaymentMethod());
        dto.setTransactionReference(payment.getTransactionReference());
        dto.setPaymentStatus(payment.getPaymentStatus());
        return dto;
    }
}
