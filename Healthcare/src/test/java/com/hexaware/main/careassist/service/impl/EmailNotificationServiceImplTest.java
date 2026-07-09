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

import com.hexaware.careassist.dto.EmailNotificationDTO;
import com.hexaware.careassist.dto.UserDTO;
import com.hexaware.careassist.service.IEmailNotificationService;
import com.hexaware.careassist.service.IUserService;

@SpringBootTest
@Transactional
class EmailNotificationServiceImplTest {

    @Autowired
    private IUserService userService;

    @Autowired
    private IEmailNotificationService emailNotificationService;

    @Test
    void createNotificationTest() {
        UserDTO user = userService.createUser(userDTO());
        EmailNotificationDTO dto = notificationDTO(user.getUserId());

        EmailNotificationDTO savedNotification = emailNotificationService.createNotification(dto);

        assertNotNull(savedNotification);
        assertTrue(savedNotification.getNotificationId() > 0);
        assertEquals("SENT", savedNotification.getStatus());
    }

    @Test
    void getNotificationsByUserIdTest() {
        UserDTO user = userService.createUser(userDTO());
        emailNotificationService.createNotification(notificationDTO(user.getUserId()));

        List<EmailNotificationDTO> notifications = emailNotificationService.getNotificationsByUserId(user.getUserId());

        assertNotNull(notifications);
        assertFalse(notifications.isEmpty());
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

    private EmailNotificationDTO notificationDTO(Integer userId) {
        EmailNotificationDTO dto = new EmailNotificationDTO();
        dto.setUserId(userId);
        dto.setSubject("Claim Update");
        dto.setMessage("Your claim status has been updated successfully");
        dto.setSentAt(LocalDateTime.now());
        dto.setStatus("SENT");
        return dto;
    }
}
