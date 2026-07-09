package com.hexaware.careassist.service;

public interface IInvoicePdfService {
    byte[] generateInvoicePdf(Integer invoiceId);
    void emailInvoicePdf(Integer invoiceId, String emailOverride);
}
