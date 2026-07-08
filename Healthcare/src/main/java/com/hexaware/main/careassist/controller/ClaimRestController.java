package com.hexaware.main.careassist.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hexaware.main.careassist.dto.ClaimDTO;
import com.hexaware.main.careassist.service.IClaimService;

import jakarta.validation.Valid;
@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/v1/claims")
public class ClaimRestController {
	
	@Autowired
	private IClaimService claimService;

	@PostMapping("/submit")
	public ClaimDTO submitClaim(@Valid @RequestBody ClaimDTO dto) {
		return claimService.submitClaim(dto);
	}

	@GetMapping("/{claimId}")
	public ClaimDTO getClaimById(@PathVariable Integer claimId) {
		return claimService.getClaimById(claimId);
	}

	@GetMapping("/patient/{patientId}")
	public List<ClaimDTO> getClaimsByPatientId(@PathVariable Integer patientId) {
		return claimService.getClaimsByPatientId(patientId);
	}

	@GetMapping("/company/{companyId}")
	public List<ClaimDTO> getClaimsByInsuranceCompanyId(@PathVariable Integer companyId) {
		return claimService.getClaimsByInsuranceCompanyId(companyId);
	}

	@GetMapping("/all")
	public List<ClaimDTO> getAllClaims() {
		return claimService.getAllClaims();
	}

	@PatchMapping("/approve/{claimId}")
	public ClaimDTO approveClaim(@PathVariable Integer claimId) {
		return claimService.approveClaim(claimId);
	}

	@PatchMapping("/reject/{claimId}")
	public ClaimDTO rejectClaim(@PathVariable Integer claimId, @RequestParam String rejectionReason) {
		return claimService.rejectClaim(claimId, rejectionReason);
	}

	@DeleteMapping("/delete/{claimId}")
	public String deleteClaim(@PathVariable Integer claimId) {
		claimService.deleteClaim(claimId);
		return "Claim deleted successfully";
	}
}
