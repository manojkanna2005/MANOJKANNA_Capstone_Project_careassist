package com.hexaware.main.careassist.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hexaware.main.careassist.dto.InvoicePaymentDTO;
import com.hexaware.main.careassist.dto.InvoicePaymentRequest;
import com.hexaware.main.careassist.service.IInvoicePaymentService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;

@RestController
@Validated
@RequestMapping("/api/v1/invoice-payments")
public class InvoicePaymentRestController {

    private final IInvoicePaymentService invoicePaymentService;

    public InvoicePaymentRestController(IInvoicePaymentService invoicePaymentService) {
        this.invoicePaymentService = invoicePaymentService;
    }

    @PostMapping("/process")
    @PreAuthorize("hasRole('PATIENT')")
    public InvoicePaymentDTO processPayment(
            @Valid @RequestBody InvoicePaymentRequest request,
            Authentication authentication) {
        return invoicePaymentService.processPayment(request, authentication.getName());
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('PATIENT')")
    public List<InvoicePaymentDTO> getMyPayments(Authentication authentication) {
        return invoicePaymentService.getMyPayments(authentication.getName());
    }

    @GetMapping("/invoice/{invoiceId}")
    @PreAuthorize("hasAnyRole('PATIENT', 'ADMIN')")
    public InvoicePaymentDTO getPaymentByInvoiceId(
            @PathVariable @Positive Integer invoiceId,
            Authentication authentication) {
        boolean admin = authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
        return invoicePaymentService.getPaymentByInvoiceId(
                invoiceId,
                authentication.getName(),
                admin);
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public List<InvoicePaymentDTO> getAllPayments() {
        return invoicePaymentService.getAllPayments();
    }
}
