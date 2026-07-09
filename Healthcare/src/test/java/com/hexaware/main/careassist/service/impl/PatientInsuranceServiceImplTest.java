package com.hexaware.main.careassist.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

import com.hexaware.careassist.dto.InsuranceCompanyDTO;
import com.hexaware.careassist.dto.InsurancePlanDTO;
import com.hexaware.careassist.dto.PatientDTO;
import com.hexaware.careassist.dto.PatientInsuranceDTO;
import com.hexaware.careassist.dto.UserDTO;
import com.hexaware.careassist.service.IInsuranceCompanyService;
import com.hexaware.careassist.service.IInsurancePlanService;
import com.hexaware.careassist.service.IPatientInsuranceService;
import com.hexaware.careassist.service.IPatientService;
import com.hexaware.careassist.service.IUserService;

@SpringBootTest
@Transactional
class PatientInsuranceServiceImplTest {

    @Autowired
    private IUserService userService;

    @Autowired
    private IPatientService patientService;

    @Autowired
    private IInsuranceCompanyService insuranceCompanyService;

    @Autowired
    private IInsurancePlanService insurancePlanService;

    @Autowired
    private IPatientInsuranceService patientInsuranceService;

    @Test
    void selectInsurancePlanTest() {
        PatientDTO patient = createPatient();
        InsurancePlanDTO plan = createPlan();
        PatientInsuranceDTO dto = patientInsuranceDTO(patient.getPatientId(), plan.getPlanId());

        PatientInsuranceDTO savedInsurance = patientInsuranceService.selectInsurancePlan(dto);

        assertNotNull(savedInsurance);
        assertTrue(savedInsurance.getEnrollmentId() > 0);
        assertEquals("ACTIVE", savedInsurance.getStatus());
    }

    @Test
    void getInsuranceHistoryByPatientIdTest() {
        PatientDTO patient = createPatient();
        InsurancePlanDTO plan = createPlan();
        patientInsuranceService.selectInsurancePlan(patientInsuranceDTO(patient.getPatientId(), plan.getPlanId()));

        List<PatientInsuranceDTO> history = patientInsuranceService.getInsuranceHistoryByPatientId(patient.getPatientId());

        assertNotNull(history);
        assertTrue(history.size() > 0);
    }

    @Test
    void cancelInsurancePlanTest() {
        PatientDTO patient = createPatient();
        InsurancePlanDTO plan = createPlan();
        PatientInsuranceDTO savedInsurance = patientInsuranceService.selectInsurancePlan(patientInsuranceDTO(patient.getPatientId(), plan.getPlanId()));

        PatientInsuranceDTO cancelled = patientInsuranceService.cancelInsurancePlan(savedInsurance.getEnrollmentId());

        assertNotNull(cancelled);
        assertEquals("CANCELLED", cancelled.getStatus());
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

    private InsurancePlanDTO createPlan() {
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
        InsuranceCompanyDTO savedCompany = insuranceCompanyService.createCompanyProfile(company);

        InsurancePlanDTO plan = new InsurancePlanDTO();
        plan.setCompanyId(savedCompany.getCompanyId());
        plan.setPlanName("Basic Health Plan");
        plan.setPlanDescription("Basic health insurance plan");
        plan.setCoverageAmount(new BigDecimal("500000"));
        plan.setPremiumAmount(new BigDecimal("12000"));
        plan.setValidityMonths(12);
        plan.setActive(true);
        return insurancePlanService.createPlan(plan);
    }

    private PatientInsuranceDTO patientInsuranceDTO(Integer patientId, Integer planId) {
        PatientInsuranceDTO dto = new PatientInsuranceDTO();
        dto.setPatientId(patientId);
        dto.setPlanId(planId);
        dto.setEnrollmentDate(LocalDate.now());
        dto.setExpiryDate(LocalDate.now().plusYears(1));
        dto.setStatus("ACTIVE");
        return dto;
    }
}
