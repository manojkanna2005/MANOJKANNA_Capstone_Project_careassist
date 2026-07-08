package com.hexaware.main.careassist.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import com.hexaware.main.careassist.entity.Invoice;

public interface InvoiceRepository extends JpaRepository<Invoice, Integer> {
    List<Invoice> findByPatientPatientId(Integer patientId);
    List<Invoice> findByHealthcareProviderProviderId(Integer providerId);
}
