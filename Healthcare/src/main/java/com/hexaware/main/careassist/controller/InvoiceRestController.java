package com.hexaware.main.careassist.controller;

import java.util.List;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hexaware.main.careassist.dto.InvoiceDTO;
import com.hexaware.main.careassist.service.IInvoicePdfService;
import com.hexaware.main.careassist.service.IInvoiceService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

@RestController
@Validated
@RequestMapping("/api/v1/invoices")
public class InvoiceRestController {

    private final IInvoiceService invoiceService;
    private final IInvoicePdfService invoicePdfService;

    public InvoiceRestController(
            IInvoiceService invoiceService,
            IInvoicePdfService invoicePdfService) {
        this.invoiceService = invoiceService;
        this.invoicePdfService = invoicePdfService;
    }

    @PostMapping("/generate")
    @PreAuthorize("hasAnyRole('PROVIDER', 'ADMIN')")
    public InvoiceDTO generateInvoice(@Valid @RequestBody InvoiceDTO dto) {
        return invoiceService.generateInvoice(dto);
    }

    @PutMapping("/update/{invoiceId}")
    @PreAuthorize("hasAnyRole('PROVIDER', 'ADMIN')")
    public InvoiceDTO updateInvoice(
            @PathVariable @Positive Integer invoiceId,
            @Valid @RequestBody InvoiceDTO dto) {
        return invoiceService.updateInvoice(invoiceId, dto);
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('PATIENT')")
    public List<InvoiceDTO> getMyInvoices(Authentication authentication) {
        return invoiceService.getMyInvoices(authentication.getName());
    }

    @GetMapping("/{invoiceId}")
    public InvoiceDTO getInvoiceById(@PathVariable @Positive Integer invoiceId) {
        return invoiceService.getInvoiceById(invoiceId);
    }

    @GetMapping("/{invoiceId}/pdf")
    public ResponseEntity<byte[]> downloadInvoicePdf(@PathVariable @Positive Integer invoiceId) {
        invoiceService.getInvoiceById(invoiceId);
        byte[] pdf = invoicePdfService.generateInvoicePdf(invoiceId);
        String fileName = "invoice-" + invoiceId + ".pdf";

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(fileName).build().toString())
                .body(pdf);
    }

    @PostMapping("/{invoiceId}/email-pdf")
    public ResponseEntity<String> emailInvoicePdf(
            @PathVariable @Positive Integer invoiceId,
            @RequestParam(required = false)
            @Email(message = "Email address is invalid")
            @Size(max = 254, message = "Email address is too long") String email) {
        invoiceService.getInvoiceById(invoiceId);
        invoicePdfService.emailInvoicePdf(invoiceId, email);
        return ResponseEntity.ok("Invoice PDF email sent successfully");
    }

    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyRole('PROVIDER', 'ADMIN')")
    public List<InvoiceDTO> getInvoicesByPatientId(@PathVariable @Positive Integer patientId) {
        return invoiceService.getInvoicesByPatientId(patientId);
    }

    @GetMapping("/provider/{providerId}")
    @PreAuthorize("hasAnyRole('PROVIDER', 'ADMIN')")
    public List<InvoiceDTO> getInvoicesByProviderId(@PathVariable @Positive Integer providerId) {
        return invoiceService.getInvoicesByProviderId(providerId);
    }

    @PatchMapping("/status/{invoiceId}")
    @PreAuthorize("hasAnyRole('PROVIDER', 'ADMIN')")
    public InvoiceDTO updateInvoiceStatus(
            @PathVariable @Positive Integer invoiceId,
            @RequestParam
            @Pattern(
                    regexp = "PENDING|UNPAID|OVERDUE|CANCELLED",
                    flags = Pattern.Flag.CASE_INSENSITIVE,
                    message = "Status must be PENDING, UNPAID, OVERDUE, or CANCELLED") String status) {
        return invoiceService.updateInvoiceStatus(invoiceId, status);
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public List<InvoiceDTO> getAllInvoices() {
        return invoiceService.getAllInvoices();
    }

    @DeleteMapping("/delete/{invoiceId}")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteInvoice(@PathVariable @Positive Integer invoiceId) {
        invoiceService.deleteInvoice(invoiceId);
        return "Invoice deleted successfully";
    }
}
