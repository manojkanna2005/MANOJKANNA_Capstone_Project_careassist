package com.hexaware.main.careassist.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hexaware.main.careassist.dto.ClaimPaymentDTO;
import com.hexaware.main.careassist.service.IClaimPaymentService;

import jakarta.validation.Valid;
@RestController
@RequestMapping("/api/v1/claim-payments")
public class ClaimPaymentRestController {
	
	@Autowired
	private IClaimPaymentService claimPaymentService;


	@PostMapping("/process")
	public ClaimPaymentDTO processClaimPayment(@Valid @RequestBody ClaimPaymentDTO dto) {
		return claimPaymentService.processClaimPayment(dto);
	}

	@GetMapping("/{paymentId}")
	public ClaimPaymentDTO getPaymentById(@PathVariable Integer paymentId) {
		return claimPaymentService.getPaymentById(paymentId);
	}

	@GetMapping("/claim/{claimId}")
	public ClaimPaymentDTO getPaymentByClaimId(@PathVariable Integer claimId) {
		return claimPaymentService.getPaymentByClaimId(claimId);
	}

	@GetMapping("/all")
	public List<ClaimPaymentDTO> getAllPayments() {
		return claimPaymentService.getAllPayments();
	}

	@DeleteMapping("/delete/{paymentId}")
	public String deletePayment(@PathVariable Integer paymentId) {
		claimPaymentService.deletePayment(paymentId);
		return "Claim payment deleted successfully";
	}
}
