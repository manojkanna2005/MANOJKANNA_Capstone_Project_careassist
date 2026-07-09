package com.hexaware.careassist.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.hexaware.careassist.dto.ClaimDTO;

public interface IClaimService {
    ClaimDTO submitClaim(ClaimDTO dto);
    ClaimDTO submitClaimWithDocuments(ClaimDTO dto, List<MultipartFile> files);
    ClaimDTO getClaimById(Integer claimId);
    List<ClaimDTO> getClaimsByPatientId(Integer patientId);
    List<ClaimDTO> getClaimsByInsuranceCompanyId(Integer companyId);
    List<ClaimDTO> getActionableClaimsByInsuranceCompanyId(Integer companyId);
    List<ClaimDTO> getAllClaims();
    ClaimDTO approveClaim(Integer claimId);
    ClaimDTO approveClaim(Integer claimId, BigDecimal approvedAmount);
    ClaimDTO rejectClaim(Integer claimId, String rejectionReason);
    void deleteClaim(Integer claimId);
}
