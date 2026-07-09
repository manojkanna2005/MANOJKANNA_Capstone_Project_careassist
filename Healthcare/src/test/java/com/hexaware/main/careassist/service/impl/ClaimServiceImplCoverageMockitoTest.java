package com.hexaware.main.careassist.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.hexaware.main.careassist.dto.ClaimDTO;
import com.hexaware.main.careassist.entity.AppUser;
import com.hexaware.main.careassist.entity.Claim;
import com.hexaware.main.careassist.entity.HealthcareProvider;
import com.hexaware.main.careassist.entity.InsuranceCompany;
import com.hexaware.main.careassist.entity.InsurancePlan;
import com.hexaware.main.careassist.entity.Invoice;
import com.hexaware.main.careassist.entity.Patient;
import com.hexaware.main.careassist.entity.PatientInsurance;
import com.hexaware.main.careassist.exception.BusinessValidationException;
import com.hexaware.main.careassist.repository.ClaimDocumentRepository;
import com.hexaware.main.careassist.repository.ClaimPaymentRepository;
import com.hexaware.main.careassist.repository.ClaimRepository;
import com.hexaware.main.careassist.repository.InsuranceCompanyRepository;
import com.hexaware.main.careassist.repository.InvoicePaymentRepository;
import com.hexaware.main.careassist.repository.InvoiceRepository;
import com.hexaware.main.careassist.repository.PatientInsuranceRepository;
import com.hexaware.main.careassist.repository.PatientRepository;
import com.hexaware.main.careassist.service.IClaimDocumentService;
import com.hexaware.main.careassist.service.IMailService;

@ExtendWith(MockitoExtension.class)
class ClaimServiceImplCoverageMockitoTest {

    @Mock
    private ClaimRepository claimRepository;

    @Mock
    private ClaimDocumentRepository claimDocumentRepository;

    @Mock
    private ClaimPaymentRepository claimPaymentRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private InvoicePaymentRepository invoicePaymentRepository;

    @Mock
    private InsuranceCompanyRepository companyRepository;

    @Mock
    private PatientInsuranceRepository patientInsuranceRepository;

    @Mock
    private IClaimDocumentService claimDocumentService;

    @Mock
    private IMailService mailService;

    @InjectMocks
    private ClaimServiceImpl service;

    private Patient patient;
    private Invoice invoice;
    private InsuranceCompany company;
    private PatientInsurance enrollment;
    private ClaimDTO dto;

    @BeforeEach
    void setUp() {

        AppUser patientUser = new AppUser();
        patientUser.setEmail("patient@example.com");

        patient = new Patient();
        patient.setPatientId(1);
        patient.setFullName("Test Patient");
        patient.setAppUser(patientUser);

        AppUser providerUser = new AppUser();
        providerUser.setEmail("provider@example.com");

        HealthcareProvider provider = new HealthcareProvider();
        provider.setProviderId(2);
        provider.setProviderName("Test Provider");
        provider.setAppUser(providerUser);

        invoice = new Invoice();
        invoice.setInvoiceId(3);
        invoice.setInvoiceNumber("INV-3");
        invoice.setPatient(patient);
        invoice.setHealthcareProvider(provider);
        invoice.setTotalAmount(new BigDecimal("5000"));
        invoice.setStatus("PENDING");

        AppUser companyUser = new AppUser();
        companyUser.setEmail("insurance@example.com");

        company = new InsuranceCompany();
        company.setCompanyId(4);
        company.setCompanyName("Test Insurance");
        company.setAppUser(companyUser);
        company.setContactEmail("insurance@example.com");

        InsurancePlan plan = new InsurancePlan();
        plan.setPlanId(5);
        plan.setPlanName("Gold Plan");
        plan.setInsuranceCompany(company);
        plan.setCoverageAmount(new BigDecimal("10000"));
        plan.setActive(true);

        enrollment = new PatientInsurance();
        enrollment.setEnrollmentId(6);
        enrollment.setPatient(patient);
        enrollment.setInsurancePlan(plan);
        enrollment.setEnrollmentDate(LocalDate.now().minusMonths(1));
        enrollment.setExpiryDate(LocalDate.now().plusMonths(11));
        enrollment.setStatus("ACTIVE");

        dto = new ClaimDTO();
        dto.setPatientId(1);
        dto.setInvoiceId(3);
        dto.setCompanyId(4);
        dto.setEnrollmentId(6);
        dto.setDiagnosis("Fever");
        dto.setTreatment("Medicine");
        dto.setDateOfService(LocalDate.now());
        dto.setClaimAmount(new BigDecimal("3000"));

        when(patientRepository.findById(1))
                .thenReturn(Optional.of(patient));

        when(invoiceRepository.findById(3))
                .thenReturn(Optional.of(invoice));

        when(companyRepository.findById(4))
                .thenReturn(Optional.of(company));

        when(patientInsuranceRepository.findById(6))
                .thenReturn(Optional.of(enrollment));
    }

    @Test
    void submitClaimReturnsCompleteHistoryFields() {

        when(claimRepository
                .existsByInvoiceInvoiceIdAndStatusIn(any(), any()))
                .thenReturn(false);

        when(claimRepository.sumApprovedAmountByEnrollmentId(6))
                .thenReturn(BigDecimal.ZERO);

        when(claimRepository.save(any(Claim.class)))
                .thenAnswer(invocation -> {
                    Claim claim = invocation.getArgument(0);
                    claim.setClaimId(10);
                    return claim;
                });

        when(claimDocumentRepository.countByClaimClaimId(10))
                .thenReturn(0L);

        ClaimDTO saved = service.submitClaim(dto);

        assertNotNull(saved);
        assertEquals(10, saved.getClaimId());
        assertEquals("Test Patient", saved.getPatientName());
        assertEquals("INV-3", saved.getInvoiceNumber());
        assertEquals(
                new BigDecimal("5000"),
                saved.getInvoiceAmount()
        );
        assertEquals("Gold Plan", saved.getPlanName());
        assertEquals(
                new BigDecimal("10000"),
                saved.getCoverageAmount()
        );
        assertEquals(
                new BigDecimal("3000"),
                saved.getClaimAmount()
        );
        assertEquals("PENDING", saved.getStatus());
    }

    @Test
    void submitClaimRejectsAmountAboveInvoiceTotal() {

        dto.setClaimAmount(new BigDecimal("6000"));

        BusinessValidationException exception = assertThrows(
                BusinessValidationException.class,
                () -> service.submitClaim(dto)
        );

        assertNotNull(exception.getFieldErrors());

        assertTrue(
                exception.getFieldErrors()
                        .containsKey("claimAmount")
        );
    }
}