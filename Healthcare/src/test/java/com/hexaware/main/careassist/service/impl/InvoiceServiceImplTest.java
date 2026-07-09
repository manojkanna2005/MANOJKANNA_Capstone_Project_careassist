package com.hexaware.main.careassist.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.hexaware.careassist.dto.HealthcareProviderDTO;
import com.hexaware.careassist.dto.InvoiceDTO;
import com.hexaware.careassist.dto.PatientDTO;
import com.hexaware.careassist.dto.UserDTO;
import com.hexaware.careassist.exception.BusinessValidationException;
import com.hexaware.careassist.service.IHealthcareProviderService;
import com.hexaware.careassist.service.IInvoiceService;
import com.hexaware.careassist.service.IPatientService;
import com.hexaware.careassist.service.IUserService;

@SpringBootTest
@Transactional
class InvoiceServiceImplTest {

    @Autowired
    private IUserService userService;

    @Autowired
    private IPatientService patientService;

    @Autowired
    private IHealthcareProviderService healthcareProviderService;

    @Autowired
    private IInvoiceService invoiceService;

    @Test
    void generateInvoiceTest() {
        PatientDTO patient = createPatient();
        HealthcareProviderDTO provider = createProvider();
        InvoiceDTO dto = invoiceDTO(patient.getPatientId(), provider.getProviderId());

        InvoiceDTO savedInvoice = invoiceService.generateInvoice(dto);

        assertNotNull(savedInvoice);
        assertTrue(savedInvoice.getInvoiceId() > 0);
        assertEquals("PENDING", savedInvoice.getStatus());
    }

    @Test
    void getInvoicesByPatientIdTest() {
        PatientDTO patient = createPatient();
        HealthcareProviderDTO provider = createProvider();
        invoiceService.generateInvoice(invoiceDTO(patient.getPatientId(), provider.getProviderId()));

        List<InvoiceDTO> invoices = invoiceService.getInvoicesByPatientId(patient.getPatientId());

        assertNotNull(invoices);
        assertFalse(invoices.isEmpty());
    }

    @Test
    void manualPaidStatusIsRejectedTest() {
        PatientDTO patient = createPatient();
        HealthcareProviderDTO provider = createProvider();
        InvoiceDTO savedInvoice = invoiceService.generateInvoice(
                invoiceDTO(patient.getPatientId(), provider.getProviderId()));

        assertThrows(
                BusinessValidationException.class,
                () -> invoiceService.updateInvoiceStatus(savedInvoice.getInvoiceId(), "PAID"));
    }

    private PatientDTO createPatient() {
        UserDTO user = new UserDTO();
        user.setUsername("patient");
        user.setEmail("patient" + UUID.randomUUID() + "@gmail.com");
        user.setPassword("Test1234");
        user.setRole("PATIENT");
        user.setPhoneNumber("9876543210");
        user.setCreatedAt(LocalDateTime.now());
        user.setActive(true);
        UserDTO savedUser = userService.createUser(user);

        PatientDTO patient = new PatientDTO();
        patient.setUserId(savedUser.getUserId());
        patient.setFullName("Manoj Kanna");
        patient.setDateOfBirth(LocalDate.of(2005, 8, 15));
        patient.setGender("MALE");
        patient.setAddress("Chennai Tamil Nadu");
        patient.setSymptoms("Fever");
        patient.setTreatment("Medicine");
        return patientService.createPatientProfile(patient);
    }

    private HealthcareProviderDTO createProvider() {
        UserDTO user = new UserDTO();
        user.setUsername("provider");
        user.setEmail("provider" + UUID.randomUUID() + "@gmail.com");
        user.setPassword("Test1234");
        user.setRole("HEALTHCARE_PROVIDER");
        user.setPhoneNumber("9876543210");
        user.setCreatedAt(LocalDateTime.now());
        user.setActive(true);
        UserDTO savedUser = userService.createUser(user);

        HealthcareProviderDTO provider = new HealthcareProviderDTO();
        provider.setUserId(savedUser.getUserId());
        provider.setProviderName("Apollo Hospital");
        provider.setSpecialization("Cardiology");
        provider.setLicenseNumber("LIC" + System.currentTimeMillis());
        provider.setAddress("Chennai Tamil Nadu");
        return healthcareProviderService.createProviderProfile(provider);
    }

    private InvoiceDTO invoiceDTO(Integer patientId, Integer providerId) {
        InvoiceDTO dto = new InvoiceDTO();
        dto.setInvoiceNumber("INV" + System.currentTimeMillis());
        dto.setPatientId(patientId);
        dto.setProviderId(providerId);
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
