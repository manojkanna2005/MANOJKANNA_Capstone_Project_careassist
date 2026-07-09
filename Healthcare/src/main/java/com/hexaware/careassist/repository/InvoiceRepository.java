package com.hexaware.careassist.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hexaware.careassist.entity.Invoice;

public interface InvoiceRepository extends JpaRepository<Invoice, Integer> {
    List<Invoice> findByPatientPatientId(Integer patientId);
    List<Invoice> findByHealthcareProviderProviderId(Integer providerId);
}
