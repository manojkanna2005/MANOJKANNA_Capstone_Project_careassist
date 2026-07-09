package com.hexaware.careassist.service;

import java.util.List;

import com.hexaware.careassist.dto.InvoicePaymentDTO;
import com.hexaware.careassist.dto.InvoicePaymentRequest;

public interface IInvoicePaymentService {

    InvoicePaymentDTO processPayment(InvoicePaymentRequest request, String authenticatedEmail);

    InvoicePaymentDTO getPaymentByInvoiceId(Integer invoiceId, String authenticatedEmail, boolean admin);

    List<InvoicePaymentDTO> getMyPayments(String authenticatedEmail);

    List<InvoicePaymentDTO> getAllPayments();
}
