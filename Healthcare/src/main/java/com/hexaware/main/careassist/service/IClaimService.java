package com.hexaware.main.careassist.service;

import java.util.List;
import com.hexaware.main.careassist.dto.ClaimDTO;

public interface IClaimService {
    ClaimDTO submitClaim(ClaimDTO dto);
    ClaimDTO getClaimById(Integer claimId);
    List<ClaimDTO> getClaimsByPatientId(Integer patientId);
    List<ClaimDTO> getClaimsByInsuranceCompanyId(Integer companyId);
    List<ClaimDTO> getAllClaims();
    ClaimDTO approveClaim(Integer claimId);
    ClaimDTO rejectClaim(Integer claimId, String rejectionReason);
    void deleteClaim(Integer claimId);
}
