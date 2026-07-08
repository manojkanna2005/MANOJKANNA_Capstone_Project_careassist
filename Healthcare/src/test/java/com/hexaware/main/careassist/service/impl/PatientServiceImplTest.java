package com.hexaware.main.careassist.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.hexaware.main.careassist.dto.PatientDTO;
import com.hexaware.main.careassist.dto.UserDTO;
import com.hexaware.main.careassist.service.IPatientService;
import com.hexaware.main.careassist.service.IUserService;

@SpringBootTest
@Transactional
class PatientServiceImplTest {

    @Autowired
    private IUserService userService;

    @Autowired
    private IPatientService patientService;

    @Test
    void createPatientProfileTest() {
        UserDTO user = userService.createUser(userDTO());
        PatientDTO dto = patientDTO(user.getUserId());

        PatientDTO savedPatient = patientService.createPatientProfile(dto);

        assertNotNull(savedPatient);
        assertTrue(savedPatient.getPatientId() > 0);
        assertEquals("Manoj Kanna", savedPatient.getFullName());
    }

    @Test
    void getPatientByUserIdTest() {
        UserDTO user = userService.createUser(userDTO());
        PatientDTO savedPatient = patientService.createPatientProfile(patientDTO(user.getUserId()));

        PatientDTO foundPatient = patientService.getPatientByUserId(user.getUserId());

        assertNotNull(foundPatient);
        assertEquals(savedPatient.getPatientId(), foundPatient.getPatientId());
    }

    @Test
    void getAllPatientsTest() {
        UserDTO user = userService.createUser(userDTO());
        patientService.createPatientProfile(patientDTO(user.getUserId()));

        List<PatientDTO> patients = patientService.getAllPatients();

        assertNotNull(patients);
        assertFalse(patients.isEmpty());
    }

    private UserDTO userDTO() {
        UserDTO dto = new UserDTO();
        dto.setUsername("patient");
        dto.setEmail("patient" + UUID.randomUUID() + "@gmail.com");
        dto.setPassword("Test1234");
        dto.setRole("PATIENT");
        dto.setPhoneNumber("9876543210");
        dto.setCreatedAt(LocalDateTime.now());
        dto.setActive(true);
        return dto;
    }

    private PatientDTO patientDTO(Integer userId) {
        PatientDTO dto = new PatientDTO();
        dto.setUserId(userId);
        dto.setFullName("Manoj Kanna");
        dto.setDateOfBirth(LocalDate.of(2005, 8, 15));
        dto.setGender("MALE");
        dto.setAddress("Chennai Tamil Nadu");
        dto.setSymptoms("Fever");
        dto.setTreatment("Medicine");
        return dto;
    }
}
