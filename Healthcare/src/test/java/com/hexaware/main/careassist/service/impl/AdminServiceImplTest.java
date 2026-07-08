package com.hexaware.main.careassist.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.hexaware.main.careassist.dto.AdminDTO;
import com.hexaware.main.careassist.dto.UserDTO;
import com.hexaware.main.careassist.service.IAdminService;
import com.hexaware.main.careassist.service.IUserService;

@SpringBootTest
@Transactional
class AdminServiceImplTest {

    @Autowired
    private IUserService userService;

    @Autowired
    private IAdminService adminService;

    @Test
    void createAdminTest() {
        UserDTO user = userService.createUser(userDTO());
        AdminDTO dto = adminDTO(user.getUserId());

        AdminDTO savedAdmin = adminService.createAdmin(dto);

        assertNotNull(savedAdmin);
        assertTrue(savedAdmin.getAdminId() > 0);
        assertEquals("System Admin", savedAdmin.getFullName());
    }

    @Test
    void getAllAdminsTest() {
        UserDTO user = userService.createUser(userDTO());
        adminService.createAdmin(adminDTO(user.getUserId()));

        List<AdminDTO> admins = adminService.getAllAdmins();

        assertNotNull(admins);
        assertFalse(admins.isEmpty());
    }

    @Test
    void updateUserStatusTest() {
        UserDTO user = userService.createUser(userDTO());

        UserDTO updatedUser = adminService.updateUserStatus(user.getUserId(), false);

        assertNotNull(updatedUser);
        assertFalse(updatedUser.isActive());
    }

    @Test
    void getDashboardSummaryTest() {
        Map<String, Object> summary = adminService.getDashboardSummary();

        assertNotNull(summary);
        assertTrue(summary.size() > 0);
    }

    private UserDTO userDTO() {
        UserDTO dto = new UserDTO();
        dto.setUsername("admin");
        dto.setEmail("admin" + UUID.randomUUID() + "@gmail.com");
        dto.setPassword("Test1234");
        dto.setRole("ADMIN");
        dto.setPhoneNumber("9876543210");
        dto.setCreatedAt(LocalDateTime.now());
        dto.setActive(true);
        return dto;
    }

    private AdminDTO adminDTO(Integer userId) {
        AdminDTO dto = new AdminDTO();
        dto.setUserId(userId);
        dto.setFullName("System Admin");
        dto.setDepartment("Claims");
        return dto;
    }
}
