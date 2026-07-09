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

import com.hexaware.careassist.dto.UserDTO;
import com.hexaware.careassist.service.IUserService;

@SpringBootTest
@Transactional
class UserServiceImplTest {

    @Autowired
    private IUserService userService;

    @Test
    void createUserTest() {
        UserDTO dto = userDTO("patient");

        UserDTO savedUser = userService.createUser(dto);

        assertNotNull(savedUser);
        assertTrue(savedUser.getUserId() > 0);
        assertEquals("patient", savedUser.getUsername());
        assertEquals("PATIENT", savedUser.getRole());
    }

    @Test
    void getUserByIdTest() {
        UserDTO savedUser = userService.createUser(userDTO("patientid"));

        UserDTO foundUser = userService.getUserById(savedUser.getUserId());

        assertNotNull(foundUser);
        assertEquals(savedUser.getUserId(), foundUser.getUserId());
    }

    @Test
    void getUsersByRoleTest() {
        userService.createUser(userDTO("provider"));

        List<UserDTO> users = userService.getUsersByRole("HEALTHCARE_PROVIDER");

        assertNotNull(users);
        assertFalse(users.isEmpty());
    }

    @Test
    void deactivateUserTest() {
        UserDTO savedUser = userService.createUser(userDTO("inactive"));

        UserDTO user = userService.deactivateUser(savedUser.getUserId());

        assertNotNull(user);
        assertFalse(user.isActive());
    }

    private UserDTO userDTO(String name) {
        UserDTO dto = new UserDTO();
        dto.setUsername(name);
        dto.setEmail(name + UUID.randomUUID() + "@gmail.com");
        dto.setPassword("Test1234");
        dto.setRole(name.equals("provider") ? "HEALTHCARE_PROVIDER" : "PATIENT");
        dto.setProfilePicture("profile.jpg");
        dto.setPhoneNumber("9876543210");
        dto.setCreatedAt(LocalDateTime.now());
        dto.setActive(true);
        return dto;
    }
}
