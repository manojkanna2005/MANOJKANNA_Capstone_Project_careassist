package com.hexaware.main.careassist.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.hexaware.careassist.dto.InsuranceCompanyDTO;
import com.hexaware.careassist.dto.UserDTO;
import com.hexaware.careassist.service.IInsuranceCompanyService;
import com.hexaware.careassist.service.IUserService;

@SpringBootTest
@Transactional
class InsuranceCompanyServiceImplTest {

    @Autowired
    private IUserService userService;

    @Autowired
    private IInsuranceCompanyService insuranceCompanyService;

    @Test
    void createCompanyProfileTest() {
        UserDTO user = userService.createUser(userDTO());
        InsuranceCompanyDTO dto = companyDTO(user.getUserId());

        InsuranceCompanyDTO savedCompany = insuranceCompanyService.createCompanyProfile(dto);

        assertNotNull(savedCompany);
        assertTrue(savedCompany.getCompanyId() > 0);
        assertEquals("Star Health", savedCompany.getCompanyName());
    }

    @Test
    void getCompanyByUserIdTest() {
        UserDTO user = userService.createUser(userDTO());
        InsuranceCompanyDTO savedCompany = insuranceCompanyService.createCompanyProfile(companyDTO(user.getUserId()));

        InsuranceCompanyDTO foundCompany = insuranceCompanyService.getCompanyByUserId(user.getUserId());

        assertNotNull(foundCompany);
        assertEquals(savedCompany.getCompanyId(), foundCompany.getCompanyId());
    }

    @Test
    void getAllCompaniesTest() {
        UserDTO user = userService.createUser(userDTO());
        insuranceCompanyService.createCompanyProfile(companyDTO(user.getUserId()));

        List<InsuranceCompanyDTO> companies = insuranceCompanyService.getAllCompanies();

        assertNotNull(companies);
        assertFalse(companies.isEmpty());
    }

    private UserDTO userDTO() {
        UserDTO dto = new UserDTO();
        dto.setUsername("company");
        dto.setEmail("company" + UUID.randomUUID() + "@gmail.com");
        dto.setPassword("Test1234");
        dto.setRole("INSURANCE_COMPANY");
        dto.setPhoneNumber("9876543210");
        dto.setCreatedAt(LocalDateTime.now());
        dto.setActive(true);
        return dto;
    }

    private InsuranceCompanyDTO companyDTO(Integer userId) {
        InsuranceCompanyDTO dto = new InsuranceCompanyDTO();
        dto.setUserId(userId);
        dto.setCompanyName("Star Health");
        dto.setRegistrationNumber("REG" + System.currentTimeMillis());
        dto.setAddress("Chennai Tamil Nadu");
        dto.setContactEmail("contact" + UUID.randomUUID() + "@gmail.com");
        return dto;
    }
}
