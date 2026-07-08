package com.hexaware.main.careassist.service.impl;

import lombok.extern.slf4j.Slf4j;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hexaware.main.careassist.dto.ClaimPaymentDTO;
import com.hexaware.main.careassist.entity.Claim;
import com.hexaware.main.careassist.entity.ClaimPayment;
import com.hexaware.main.careassist.exception.ResourceNotFoundException;
import com.hexaware.main.careassist.repository.ClaimPaymentRepository;
import com.hexaware.main.careassist.repository.ClaimRepository;
import com.hexaware.main.careassist.service.IClaimPaymentService;
import com.hexaware.main.careassist.service.IMailService;

@Service
@Transactional
@Slf4j
public class ClaimPaymentServiceImpl implements IClaimPaymentService {

	@Autowired
	private ClaimPaymentRepository paymentRepository;
	@Autowired
	private ClaimRepository claimRepository;

    @Autowired
    private IMailService mailService;

	@Override
	public ClaimPaymentDTO processClaimPayment(ClaimPaymentDTO dto) {
		Claim claim = getClaim(dto.getClaimId());
		if (!"APPROVED".equalsIgnoreCase(claim.getStatus())) {
			throw new IllegalStateException("Payment can be processed only for approved claims.");
		}
		ClaimPayment payment = paymentToEntity(dto);
		if (payment.getPaymentDate() == null) {
			payment.setPaymentDate(LocalDateTime.now());
		}
		ClaimPayment savedPayment = paymentRepository.save(payment);
        sendClaimPaymentEmail(savedPayment);
		return paymentToDTO(savedPayment);
	}

	@Override
	public ClaimPaymentDTO getPaymentById(Integer paymentId) {
		return paymentToDTO(getPaymentEntity(paymentId));
	}

	@Override
	public ClaimPaymentDTO getPaymentByClaimId(Integer claimId) {
		ClaimPayment payment = paymentRepository.findByClaimClaimId(claimId)
				.orElseThrow(() -> new ResourceNotFoundException("Payment not found for claim id: " + claimId));
		return paymentToDTO(payment);
	}

	@Override
	public List<ClaimPaymentDTO> getAllPayments() {
		return paymentRepository.findAll().stream().map(this::paymentToDTO).collect(Collectors.toList());
	}

	@Override
	public void deletePayment(Integer paymentId) {
		paymentRepository.delete(getPaymentEntity(paymentId));
	}

	private ClaimPayment getPaymentEntity(Integer paymentId) {
		return paymentRepository.findById(paymentId)
				.orElseThrow(() -> new ResourceNotFoundException("Claim payment not found with id: " + paymentId));
	}

	private Claim getClaim(Integer claimId) {
		return claimRepository.findById(claimId)
				.orElseThrow(() -> new ResourceNotFoundException("Claim not found with id: " + claimId));
	}

    private void sendClaimPaymentEmail(ClaimPayment payment) {
        try {
            Claim claim = payment.getClaim();
            String patientEmail = claim.getPatient().getAppUser().getEmail();
            String companyName = getInsuranceDisplayName(claim);
            String companyReplyTo = getInsuranceReplyToEmail(claim);

            mailService.sendSimpleEmail(
                    patientEmail,
                    "CareAssist claim payment processed",
                    "Your claim payment has been processed." + System.lineSeparator()
                            + "Claim ID: " + claim.getClaimId() + System.lineSeparator()
                            + "Payment amount: " + payment.getPaymentAmount() + System.lineSeparator()
                            + "Transaction reference: " + payment.getTransactionReference(),
                    "CareAssist - " + companyName,
                    companyReplyTo);

            log.info("Claim payment email sent paymentId={} claimId={} patientEmail={} replyTo={}",
                    payment.getPaymentId(), claim.getClaimId(), patientEmail, companyReplyTo);
        } catch (Exception ex) {
            log.error("Unable to send claim payment email paymentId={}", payment.getPaymentId(), ex);
        }
    }

    private String getInsuranceDisplayName(Claim claim) {
        if (claim.getInsuranceCompany() != null && claim.getInsuranceCompany().getCompanyName() != null
                && !claim.getInsuranceCompany().getCompanyName().isBlank()) {
            return claim.getInsuranceCompany().getCompanyName();
        }
        return "Insurance Company";
    }

    private String getInsuranceReplyToEmail(Claim claim) {
        if (claim.getInsuranceCompany() == null) {
            return null;
        }
        if (claim.getInsuranceCompany().getContactEmail() != null && !claim.getInsuranceCompany().getContactEmail().isBlank()) {
            return claim.getInsuranceCompany().getContactEmail();
        }
        if (claim.getInsuranceCompany().getAppUser() != null && claim.getInsuranceCompany().getAppUser().getEmail() != null
                && !claim.getInsuranceCompany().getAppUser().getEmail().isBlank()) {
            return claim.getInsuranceCompany().getAppUser().getEmail();
        }
        return null;
    }

	private ClaimPaymentDTO paymentToDTO(ClaimPayment payment) {
		ClaimPaymentDTO dto = new ClaimPaymentDTO();
		dto.setPaymentId(payment.getPaymentId());
		dto.setClaimId(payment.getClaim().getClaimId());
		dto.setPaymentDate(payment.getPaymentDate());
		dto.setPaymentAmount(payment.getPaymentAmount());
		dto.setPaymentMode(payment.getPaymentMode());
		dto.setTransactionReference(payment.getTransactionReference());
		return dto;
	}

	private ClaimPayment paymentToEntity(ClaimPaymentDTO dto) {
		ClaimPayment payment = new ClaimPayment();
		payment.setPaymentId(dto.getPaymentId());
		payment.setClaim(getClaim(dto.getClaimId()));
		payment.setPaymentDate(dto.getPaymentDate());
		payment.setPaymentAmount(dto.getPaymentAmount());
		payment.setPaymentMode(dto.getPaymentMode());
		payment.setTransactionReference(dto.getTransactionReference());
		return payment;
	}
}
