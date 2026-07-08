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

import com.hexaware.main.careassist.dto.HealthcareProviderDTO;
import com.hexaware.main.careassist.dto.UserDTO;
import com.hexaware.main.careassist.service.IHealthcareProviderService;
import com.hexaware.main.careassist.service.IUserService;

@SpringBootTest
@Transactional
class HealthcareProviderServiceImplTest {

    @Autowired
    private IUserService userService;

    @Autowired
    private IHealthcareProviderService healthcareProviderService;

    @Test
    void createProviderProfileTest() {
        UserDTO user = userService.createUser(userDTO());
        HealthcareProviderDTO dto = providerDTO(user.getUserId());

        HealthcareProviderDTO savedProvider = healthcareProviderService.createProviderProfile(dto);

        assertNotNull(savedProvider);
        assertTrue(savedProvider.getProviderId() > 0);
        assertEquals("Apollo Hospital", savedProvider.getProviderName());
    }

    @Test
    void getProviderByUserIdTest() {
        UserDTO user = userService.createUser(userDTO());
        HealthcareProviderDTO savedProvider = healthcareProviderService.createProviderProfile(providerDTO(user.getUserId()));

        HealthcareProviderDTO foundProvider = healthcareProviderService.getProviderByUserId(user.getUserId());

        assertNotNull(foundProvider);
        assertEquals(savedProvider.getProviderId(), foundProvider.getProviderId());
    }

    @Test
    void getAllProvidersTest() {
        UserDTO user = userService.createUser(userDTO());
        healthcareProviderService.createProviderProfile(providerDTO(user.getUserId()));

        List<HealthcareProviderDTO> providers = healthcareProviderService.getAllProviders();

        assertNotNull(providers);
        assertFalse(providers.isEmpty());
    }

    private UserDTO userDTO() {
        UserDTO dto = new UserDTO();
        dto.setUsername("provider");
        dto.setEmail("provider" + UUID.randomUUID() + "@gmail.com");
        dto.setPassword("Test1234");
        dto.setRole("HEALTHCARE_PROVIDER");
        dto.setPhoneNumber("9876543210");
        dto.setCreatedAt(LocalDateTime.now());
        dto.setActive(true);
        return dto;
    }

    private HealthcareProviderDTO providerDTO(Integer userId) {
        HealthcareProviderDTO dto = new HealthcareProviderDTO();
        dto.setUserId(userId);
        dto.setProviderName("Apollo Hospital");
        dto.setSpecialization("Cardiology");
        dto.setLicenseNumber("LIC" + System.currentTimeMillis());
        dto.setAddress("Chennai Tamil Nadu");
        return dto;
    }
}
