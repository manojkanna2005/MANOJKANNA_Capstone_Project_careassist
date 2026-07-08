package com.hexaware.main.careassist.service;

import java.util.List;
import com.hexaware.main.careassist.dto.InvoiceDTO;

public interface IInvoiceService {
    InvoiceDTO generateInvoice(InvoiceDTO dto);
    InvoiceDTO updateInvoice(Integer invoiceId, InvoiceDTO dto);
    InvoiceDTO getInvoiceById(Integer invoiceId);
    List<InvoiceDTO> getInvoicesByPatientId(Integer patientId);
    List<InvoiceDTO> getInvoicesByProviderId(Integer providerId);
    InvoiceDTO markInvoiceAsPaid(Integer invoiceId);
    InvoiceDTO updateInvoiceStatus(Integer invoiceId, String status);
    List<InvoiceDTO> getAllInvoices();
    void deleteInvoice(Integer invoiceId);
}
