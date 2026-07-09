package com.hexaware.careassist.service;

import java.util.List;

import com.hexaware.careassist.dto.InvoiceDTO;

public interface IInvoiceService {
    InvoiceDTO generateInvoice(InvoiceDTO dto);
    InvoiceDTO updateInvoice(Integer invoiceId, InvoiceDTO dto);
    InvoiceDTO getInvoiceById(Integer invoiceId);
    List<InvoiceDTO> getInvoicesByPatientId(Integer patientId);
    List<InvoiceDTO> getMyInvoices(String authenticatedEmail);
    List<InvoiceDTO> getInvoicesByProviderId(Integer providerId);
    InvoiceDTO updateInvoiceStatus(Integer invoiceId, String status);
    List<InvoiceDTO> getAllInvoices();
    void deleteInvoice(Integer invoiceId);
}
