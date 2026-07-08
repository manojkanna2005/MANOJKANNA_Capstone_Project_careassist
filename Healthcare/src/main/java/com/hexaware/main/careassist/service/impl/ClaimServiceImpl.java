package com.hexaware.main.careassist.service.impl;

import lombok.extern.slf4j.Slf4j;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hexaware.main.careassist.dto.ClaimDTO;
import com.hexaware.main.careassist.entity.Claim;
import com.hexaware.main.careassist.entity.InsuranceCompany;
import com.hexaware.main.careassist.entity.Invoice;
import com.hexaware.main.careassist.entity.Patient;
import com.hexaware.main.careassist.exception.ResourceNotFoundException;
import com.hexaware.main.careassist.repository.ClaimRepository;
import com.hexaware.main.careassist.repository.InsuranceCompanyRepository;
import com.hexaware.main.careassist.repository.InvoiceRepository;
import com.hexaware.main.careassist.repository.PatientRepository;
import com.hexaware.main.careassist.service.IClaimService;
import com.hexaware.main.careassist.service.IMailService;

@Service
@Transactional
@Slf4j
public class ClaimServiceImpl implements IClaimService {

	@Autowired
    private ClaimRepository claimRepository;
    
	@Autowired
	private PatientRepository patientRepository;
    
    @Autowired
    private InvoiceRepository invoiceRepository;
    
    @Autowired
    private InsuranceCompanyRepository companyRepository;

    @Autowired
    private IMailService mailService;

    @Override
    public ClaimDTO submitClaim(ClaimDTO dto) {
        Claim claim = claimToEntity(dto);
        if (claim.getSubmissionDate() == null) {
            claim.setSubmissionDate(LocalDateTime.now());
        }
        if (claim.getStatus() == null || claim.getStatus().isBlank()) {
            claim.setStatus("PENDING");
        }
        return claimToDTO(claimRepository.save(claim));
    }

    @Override
    public ClaimDTO getClaimById(Integer claimId) {
        return claimToDTO(getClaimEntity(claimId));
    }

    @Override
    public List<ClaimDTO> getClaimsByPatientId(Integer patientId) {
        return claimRepository.findByPatientPatientId(patientId).stream().map(this::claimToDTO).collect(Collectors.toList());
    }

    @Override
    public List<ClaimDTO> getClaimsByInsuranceCompanyId(Integer companyId) {
        return claimRepository.findByInsuranceCompanyCompanyId(companyId).stream().map(this::claimToDTO).collect(Collectors.toList());
    }

    @Override
    public List<ClaimDTO> getAllClaims() {
        return claimRepository.findAll().stream().map(this::claimToDTO).collect(Collectors.toList());
    }

    @Override
    public ClaimDTO approveClaim(Integer claimId) {
        Claim claim = getClaimEntity(claimId);
        claim.setStatus("APPROVED");
        claim.setApprovalDate(LocalDateTime.now());
        claim.setRejectionReason(null);
        Claim savedClaim = claimRepository.save(claim);
        sendClaimStatusEmail(savedClaim, "approved", "Your claim has been approved by the insurance company.");
        return claimToDTO(savedClaim);
    }

    @Override
    public ClaimDTO rejectClaim(Integer claimId, String rejectionReason) {
        Claim claim = getClaimEntity(claimId);
        claim.setStatus("REJECTED");
        claim.setApprovalDate(null);
        claim.setRejectionReason(rejectionReason);
        Claim savedClaim = claimRepository.save(claim);
        sendClaimStatusEmail(savedClaim, "rejected", "Your claim has been rejected. Reason: " + rejectionReason);
        return claimToDTO(savedClaim);
    }

    @Override
    public void deleteClaim(Integer claimId) {
        claimRepository.delete(getClaimEntity(claimId));
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

            log.info("Claim status email sent claimId={} status={} patientEmail={} replyTo={}",
                    claim.getClaimId(), statusText, patientEmail, companyReplyTo);
        } catch (Exception ex) {
            log.error("Unable to send claim status email claimId={} status={}", claim.getClaimId(), statusText, ex);
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

    private ClaimDTO claimToDTO(Claim claim) {
        ClaimDTO dto = new ClaimDTO();
        dto.setClaimId(claim.getClaimId());
        dto.setPatientId(claim.getPatient().getPatientId());
        dto.setInvoiceId(claim.getInvoice().getInvoiceId());
        dto.setCompanyId(claim.getInsuranceCompany().getCompanyId());
        dto.setDiagnosis(claim.getDiagnosis());
        dto.setTreatment(claim.getTreatment());
        dto.setDateOfService(claim.getDateOfService());
        dto.setClaimAmount(claim.getClaimAmount());
        dto.setSubmissionDate(claim.getSubmissionDate());
        dto.setApprovalDate(claim.getApprovalDate());
        dto.setStatus(claim.getStatus());
        dto.setRejectionReason(claim.getRejectionReason());
        return dto;
    }

    private Claim claimToEntity(ClaimDTO dto) {
        Claim claim = new Claim();
        claim.setClaimId(dto.getClaimId());
        claim.setPatient(getPatient(dto.getPatientId()));
        claim.setInvoice(getInvoice(dto.getInvoiceId()));
        claim.setInsuranceCompany(getCompany(dto.getCompanyId()));
        claim.setDiagnosis(dto.getDiagnosis());
        claim.setTreatment(dto.getTreatment());
        claim.setDateOfService(dto.getDateOfService());
        claim.setClaimAmount(dto.getClaimAmount());
        claim.setSubmissionDate(dto.getSubmissionDate());
        claim.setApprovalDate(dto.getApprovalDate());
        claim.setStatus(dto.getStatus());
        claim.setRejectionReason(dto.getRejectionReason());
        return claim;
    }
}
