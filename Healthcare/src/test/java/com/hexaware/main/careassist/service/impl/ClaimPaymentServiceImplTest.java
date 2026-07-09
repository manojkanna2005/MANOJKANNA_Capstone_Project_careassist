package com.hexaware.main.careassist.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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

import com.hexaware.main.careassist.dto.ClaimDTO;
import com.hexaware.main.careassist.dto.ClaimPaymentDTO;
import com.hexaware.main.careassist.dto.HealthcareProviderDTO;
import com.hexaware.main.careassist.dto.InsuranceCompanyDTO;
import com.hexaware.main.careassist.dto.InvoiceDTO;
import com.hexaware.main.careassist.dto.InsurancePlanDTO;
import com.hexaware.main.careassist.dto.PatientInsuranceDTO;
import com.hexaware.main.careassist.dto.PatientDTO;
import com.hexaware.main.careassist.dto.UserDTO;
import com.hexaware.main.careassist.service.IClaimPaymentService;
import com.hexaware.main.careassist.service.IClaimService;
import com.hexaware.main.careassist.service.IHealthcareProviderService;
import com.hexaware.main.careassist.service.IInsuranceCompanyService;
import com.hexaware.main.careassist.service.IInvoiceService;
import com.hexaware.main.careassist.service.IInsurancePlanService;
import com.hexaware.main.careassist.service.IPatientInsuranceService;
import com.hexaware.main.careassist.service.IPatientService;
import com.hexaware.main.careassist.service.IUserService;

@SpringBootTest
@Transactional
class ClaimPaymentServiceImplTest {

    @Autowired
    private IUserService userService;

    @Autowired
    private IPatientService patientService;

    @Autowired
    private IHealthcareProviderService healthcareProviderService;

    @Autowired
    private IInsuranceCompanyService insuranceCompanyService;

    @Autowired
    private IInvoiceService invoiceService;

    @Autowired
    private IInsurancePlanService insurancePlanService;

    @Autowired
    private IPatientInsuranceService patientInsuranceService;

    @Autowired
    private IClaimService claimService;

    @Autowired
    private IClaimPaymentService claimPaymentService;

    @Test
    void processClaimPaymentTest() {
        ClaimDTO claim = createApprovedClaim();
        ClaimPaymentDTO dto = paymentDTO(claim.getClaimId());

        ClaimPaymentDTO savedPayment = claimPaymentService.processClaimPayment(dto);

        assertNotNull(savedPayment);
        assertTrue(savedPayment.getPaymentId() > 0);
        assertEquals(claim.getClaimId(), savedPayment.getClaimId());
    }

    @Test
    void getPaymentByClaimIdTest() {
        ClaimDTO claim = createApprovedClaim();
        ClaimPaymentDTO savedPayment = claimPaymentService.processClaimPayment(paymentDTO(claim.getClaimId()));

        ClaimPaymentDTO foundPayment = claimPaymentService.getPaymentByClaimId(claim.getClaimId());

        assertNotNull(foundPayment);
        assertEquals(savedPayment.getPaymentId(), foundPayment.getPaymentId());
    }

    @Test
    void getAllPaymentsTest() {
        ClaimDTO claim = createApprovedClaim();
        ClaimPaymentDTO savedPayment = claimPaymentService.processClaimPayment(
                paymentDTO(claim.getClaimId()));

        List<ClaimPaymentDTO> payments = claimPaymentService.getAllPayments();

        assertNotNull(payments);
        assertFalse(payments.isEmpty());
        assertTrue(payments.stream().anyMatch(
                payment -> payment.getPaymentId() == savedPayment.getPaymentId()));
    }

    private ClaimDTO createApprovedClaim() {
        PatientDTO patient = createPatient();
        HealthcareProviderDTO provider = createProvider();
        InsuranceCompanyDTO company = createCompany();
        InsurancePlanDTO plan = createPlan(company.getCompanyId());
        PatientInsuranceDTO enrollment = patientInsuranceService.selectInsurancePlan(
                patientInsuranceDTO(patient.getPatientId(), plan.getPlanId()));
        InvoiceDTO invoice = invoiceService.generateInvoice(invoiceDTO(patient.getPatientId(), provider.getProviderId()));

        ClaimDTO claim = new ClaimDTO();
        claim.setPatientId(patient.getPatientId());
        claim.setInvoiceId(invoice.getInvoiceId());
        claim.setCompanyId(company.getCompanyId());
        claim.setEnrollmentId(enrollment.getEnrollmentId());
        claim.setDiagnosis("Fever");
        claim.setTreatment("Medicine");
        claim.setDateOfService(LocalDate.now());
        claim.setClaimAmount(new BigDecimal("3000"));
        claim.setSubmissionDate(LocalDateTime.now());
        claim.setStatus("PENDING");
        ClaimDTO savedClaim = claimService.submitClaim(claim);
        return claimService.approveClaim(savedClaim.getClaimId());
    }

    private ClaimPaymentDTO paymentDTO(Integer claimId) {
        ClaimPaymentDTO dto = new ClaimPaymentDTO();
        dto.setClaimId(claimId);
        dto.setPaymentDate(LocalDateTime.now());
        dto.setPaymentAmount(new BigDecimal("3000"));
        dto.setPaymentMode("UPI");
        dto.setTransactionReference("TXN" + System.currentTimeMillis());
        return dto;
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

    private InsuranceCompanyDTO createCompany() {
        UserDTO user = new UserDTO();
        user.setUsername("company");
        user.setEmail("company" + UUID.randomUUID() + "@gmail.com");
        user.setPassword("Test1234");
        user.setRole("INSURANCE_COMPANY");
        user.setPhoneNumber("9876543210");
        user.setCreatedAt(LocalDateTime.now());
        user.setActive(true);
        UserDTO savedUser = userService.createUser(user);

        InsuranceCompanyDTO company = new InsuranceCompanyDTO();
        company.setUserId(savedUser.getUserId());
        company.setCompanyName("Star Health");
        company.setRegistrationNumber("REG" + System.currentTimeMillis());
        company.setAddress("Chennai Tamil Nadu");
        company.setContactEmail("contact" + UUID.randomUUID() + "@gmail.com");
        return insuranceCompanyService.createCompanyProfile(company);
    }

    private InsurancePlanDTO createPlan(Integer companyId) {
        InsurancePlanDTO plan = new InsurancePlanDTO();
        plan.setCompanyId(companyId);
        plan.setPlanName("Test Health Plan");
        plan.setPlanDescription("Test plan used for claim coverage validation");
        plan.setCoverageAmount(new BigDecimal("500000"));
        plan.setPremiumAmount(new BigDecimal("10000"));
        plan.setValidityMonths(12);
        plan.setActive(true);
        return insurancePlanService.createPlan(plan);
    }

    private PatientInsuranceDTO patientInsuranceDTO(Integer patientId, Integer planId) {
        PatientInsuranceDTO dto = new PatientInsuranceDTO();
        dto.setPatientId(patientId);
        dto.setPlanId(planId);
        LocalDate enrollmentDate = LocalDate.now().minusDays(1);
        dto.setEnrollmentDate(enrollmentDate);
        dto.setExpiryDate(enrollmentDate.plusMonths(12));
        dto.setStatus("ACTIVE");
        return dto;
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
