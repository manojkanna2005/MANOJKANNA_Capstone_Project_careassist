package com.hexaware.main.careassist.service;

import java.util.List;

import com.hexaware.main.careassist.dto.InvoicePaymentDTO;
import com.hexaware.main.careassist.dto.InvoicePaymentRequest;

public interface IInvoicePaymentService {

    InvoicePaymentDTO processPayment(InvoicePaymentRequest request, String authenticatedEmail);

    InvoicePaymentDTO getPaymentByInvoiceId(Integer invoiceId, String authenticatedEmail, boolean admin);

    List<InvoicePaymentDTO> getMyPayments(String authenticatedEmail);

    List<InvoicePaymentDTO> getAllPayments();
}
