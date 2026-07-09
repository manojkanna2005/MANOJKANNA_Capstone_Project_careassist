package com.hexaware.main.careassist.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hexaware.main.careassist.entity.InvoicePayment;

public interface InvoicePaymentRepository extends JpaRepository<InvoicePayment, Integer> {

    Optional<InvoicePayment> findByInvoiceInvoiceId(Integer invoiceId);

    boolean existsByInvoiceInvoiceId(Integer invoiceId);

    List<InvoicePayment> findByInvoicePatientPatientIdOrderByPaymentDateDesc(Integer patientId);
}
