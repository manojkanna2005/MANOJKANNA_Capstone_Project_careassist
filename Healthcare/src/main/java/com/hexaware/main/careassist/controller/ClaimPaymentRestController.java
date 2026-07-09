package com.hexaware.main.careassist.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.validation.annotation.Validated;

import com.hexaware.main.careassist.dto.ClaimPaymentDTO;
import com.hexaware.main.careassist.service.IClaimPaymentService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;

@RestController
@Validated
@RequestMapping("/api/v1/claim-payments")
@RequiredArgsConstructor
public class ClaimPaymentRestController {

    private final IClaimPaymentService claimPaymentService;

    @PostMapping("/process")
    @PreAuthorize("hasAnyRole('INSURANCE', 'ADMIN')")
    public ClaimPaymentDTO processClaimPayment(@Valid @RequestBody ClaimPaymentDTO dto) {
        return claimPaymentService.processClaimPayment(dto);
    }

    @GetMapping("/{paymentId}")
    @PreAuthorize("hasAnyRole('INSURANCE', 'ADMIN')")
    public ClaimPaymentDTO getPaymentById(@PathVariable @Positive Integer paymentId) {
        return claimPaymentService.getPaymentById(paymentId);
    }

    @GetMapping("/claim/{claimId}")
    @PreAuthorize("hasAnyRole('INSURANCE', 'ADMIN')")
    public ClaimPaymentDTO getPaymentByClaimId(@PathVariable @Positive Integer claimId) {
        return claimPaymentService.getPaymentByClaimId(claimId);
    }

    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('INSURANCE', 'ADMIN')")
    public List<ClaimPaymentDTO> getAllPayments() {
        return claimPaymentService.getAllPayments();
    }
}
