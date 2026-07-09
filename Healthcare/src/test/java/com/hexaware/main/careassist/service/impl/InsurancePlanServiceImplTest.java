package com.hexaware.main.careassist.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.hexaware.careassist.dto.InsuranceCompanyDTO;
import com.hexaware.careassist.dto.InsurancePlanDTO;
import com.hexaware.careassist.dto.UserDTO;
import com.hexaware.careassist.service.IInsuranceCompanyService;
import com.hexaware.careassist.service.IInsurancePlanService;
import com.hexaware.careassist.service.IUserService;

@SpringBootTest
@Transactional
class InsurancePlanServiceImplTest {

    @Autowired
    private IUserService userService;

    @Autowired
    private IInsuranceCompanyService insuranceCompanyService;

    @Autowired
    private IInsurancePlanService insurancePlanService;

    @Test
    void createPlanTest() {
        InsuranceCompanyDTO company = createCompany();
        InsurancePlanDTO dto = planDTO(company.getCompanyId());

        InsurancePlanDTO savedPlan = insurancePlanService.createPlan(dto);

        assertNotNull(savedPlan);
        assertTrue(savedPlan.getPlanId() > 0);
        assertEquals("Basic Health Plan", savedPlan.getPlanName());
    }

    @Test
    void getPlansByCompanyIdTest() {
        InsuranceCompanyDTO company = createCompany();
        insurancePlanService.createPlan(planDTO(company.getCompanyId()));

        List<InsurancePlanDTO> plans = insurancePlanService.getPlansByCompanyId(company.getCompanyId());

        assertNotNull(plans);
        assertFalse(plans.isEmpty());
    }

    @Test
    void deactivatePlanTest() {
        InsuranceCompanyDTO company = createCompany();
        InsurancePlanDTO savedPlan = insurancePlanService.createPlan(planDTO(company.getCompanyId()));

        InsurancePlanDTO plan = insurancePlanService.deactivatePlan(savedPlan.getPlanId());

        assertNotNull(plan);
        assertFalse(plan.isActive());
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

    private InsurancePlanDTO planDTO(Integer companyId) {
        InsurancePlanDTO dto = new InsurancePlanDTO();
        dto.setCompanyId(companyId);
        dto.setPlanName("Basic Health Plan");
        dto.setPlanDescription("Basic health insurance plan");
        dto.setCoverageAmount(new BigDecimal("500000"));
        dto.setPremiumAmount(new BigDecimal("12000"));
        dto.setValidityMonths(12);
        dto.setActive(true);
        return dto;
    }
}
