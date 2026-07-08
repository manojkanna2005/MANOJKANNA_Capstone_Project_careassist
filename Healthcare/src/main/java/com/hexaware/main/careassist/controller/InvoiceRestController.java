package com.hexaware.main.careassist.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

@RestController
@RequestMapping("/api/v1/invoices")
public class InvoiceRestController {

    @Autowired
    private IInvoiceService invoiceService;

    @Autowired
    private IInvoicePdfService invoicePdfService;

    @PostMapping("/generate")
    public InvoiceDTO generateInvoice(@Valid @RequestBody InvoiceDTO dto) {
        return invoiceService.generateInvoice(dto);
    }

    @PutMapping("/update/{invoiceId}")
    public InvoiceDTO updateInvoice(@PathVariable Integer invoiceId, @Valid @RequestBody InvoiceDTO dto) {
        return invoiceService.updateInvoice(invoiceId, dto);
    }

    @GetMapping("/{invoiceId}")
    public InvoiceDTO getInvoiceById(@PathVariable Integer invoiceId) {
        return invoiceService.getInvoiceById(invoiceId);
    }

    @GetMapping("/{invoiceId}/pdf")
    public ResponseEntity<byte[]> downloadInvoicePdf(@PathVariable Integer invoiceId) {
        byte[] pdf = invoicePdfService.generateInvoicePdf(invoiceId);
        String fileName = "invoice-" + invoiceId + ".pdf";

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(fileName).build().toString())
                .body(pdf);
    }

    @PostMapping("/{invoiceId}/email-pdf")
    public ResponseEntity<String> emailInvoicePdf(@PathVariable Integer invoiceId,
            @RequestParam(required = false) String email) {
        invoicePdfService.emailInvoicePdf(invoiceId, email);
        return ResponseEntity.ok("Invoice PDF email sent successfully");
    }

    @GetMapping("/patient/{patientId}")
    public List<InvoiceDTO> getInvoicesByPatientId(@PathVariable Integer patientId) {
        return invoiceService.getInvoicesByPatientId(patientId);
    }

    @GetMapping("/provider/{providerId}")
    public List<InvoiceDTO> getInvoicesByProviderId(@PathVariable Integer providerId) {
        return invoiceService.getInvoicesByProviderId(providerId);
    }

    @PatchMapping("/pay/{invoiceId}")
    public InvoiceDTO markInvoiceAsPaid(@PathVariable Integer invoiceId) {
        return invoiceService.markInvoiceAsPaid(invoiceId);
    }

    @PatchMapping("/status/{invoiceId}")
    public InvoiceDTO updateInvoiceStatus(@PathVariable Integer invoiceId, @RequestParam String status) {
        return invoiceService.updateInvoiceStatus(invoiceId, status);
    }

    @GetMapping("/all")
    public List<InvoiceDTO> getAllInvoices() {
        return invoiceService.getAllInvoices();
    }

    @DeleteMapping("/delete/{invoiceId}")
    public String deleteInvoice(@PathVariable Integer invoiceId) {
        invoiceService.deleteInvoice(invoiceId);
        return "Invoice deleted successfully";
    }
}
