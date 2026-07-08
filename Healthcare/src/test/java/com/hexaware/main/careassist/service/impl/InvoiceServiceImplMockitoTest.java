package com.hexaware.main.careassist.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.hexaware.main.careassist.dto.InvoiceDTO;
import com.hexaware.main.careassist.entity.HealthcareProvider;
import com.hexaware.main.careassist.entity.Invoice;
import com.hexaware.main.careassist.entity.Patient;
import com.hexaware.main.careassist.repository.HealthcareProviderRepository;
import com.hexaware.main.careassist.repository.InvoiceRepository;
import com.hexaware.main.careassist.repository.PatientRepository;

@ExtendWith(MockitoExtension.class)
class InvoiceServiceImplMockitoTest {

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private HealthcareProviderRepository providerRepository;

    @Mock
    private PatientRepository patientRepository;

    @InjectMocks
    private InvoiceServiceImpl invoiceService;

    @Test
    void generateInvoiceShouldCalculateTaxAndTotalWithMockedRepositories() {
        Patient patient = new Patient();
        patient.setPatientId(10);

        HealthcareProvider provider = new HealthcareProvider();
        provider.setProviderId(20);

        when(patientRepository.findById(10)).thenReturn(Optional.of(patient));
        when(providerRepository.findById(20)).thenReturn(Optional.of(provider));
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(invocation -> {
            Invoice invoice = invocation.getArgument(0);
            invoice.setInvoiceId(100);
            return invoice;
        });

        InvoiceDTO saved = invoiceService.generateInvoice(invoiceDTO());

        assertEquals(100, saved.getInvoiceId());
        assertEquals(new BigDecimal("400.00"), saved.getTaxAmount());
        assertEquals(new BigDecimal("5400.00"), saved.getTotalAmount());
        assertEquals("PENDING", saved.getStatus());
    }

    private InvoiceDTO invoiceDTO() {
        InvoiceDTO dto = new InvoiceDTO();
        dto.setInvoiceNumber("INV-MOCK-001");
        dto.setPatientId(10);
        dto.setProviderId(20);
        dto.setInvoiceDate(LocalDate.now());
        dto.setDueDate(LocalDate.now().plusDays(30));
        dto.setConsultationFee(new BigDecimal("500"));
        dto.setDiagnosticTestsFee(new BigDecimal("1000"));
        dto.setDiagnosticScanFee(new BigDecimal("2000"));
        dto.setMedicationsFee(new BigDecimal("1500"));
        dto.setTaxPercentage(new BigDecimal("8"));
        dto.setStatus("PENDING");
        dto.setCreatedAt(LocalDateTime.now());
        return dto;
    }
}
