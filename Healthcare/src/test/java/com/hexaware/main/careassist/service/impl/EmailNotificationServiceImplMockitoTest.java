package com.hexaware.main.careassist.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.hexaware.careassist.dto.EmailNotificationDTO;
import com.hexaware.careassist.entity.AppUser;
import com.hexaware.careassist.entity.EmailNotification;
import com.hexaware.careassist.repository.AppUserRepository;
import com.hexaware.careassist.repository.EmailNotificationRepository;
import com.hexaware.careassist.service.IMailService;
import com.hexaware.careassist.service.impl.EmailNotificationServiceImpl;

@ExtendWith(MockitoExtension.class)
class EmailNotificationServiceImplMockitoTest {

    @Mock
    private EmailNotificationRepository notificationRepository;

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private IMailService mailService;

    @InjectMocks
    private EmailNotificationServiceImpl notificationService;

    @Test
    void createNotificationShouldSendMailAndMarkStatusSent() {
        AppUser user = new AppUser();
        user.setUserId(1);
        user.setEmail("patient@gmail.com");

        when(appUserRepository.findById(1)).thenReturn(Optional.of(user));
        when(notificationRepository.save(any(EmailNotification.class))).thenAnswer(invocation -> {
            EmailNotification notification = invocation.getArgument(0);
            notification.setNotificatoinId(5);
            return notification;
        });

        EmailNotificationDTO dto = new EmailNotificationDTO();
        dto.setUserId(1);
        dto.setSubject("Claim Update");
        dto.setMessage("Your claim was approved successfully");
        dto.setSentAt(LocalDateTime.now());
        dto.setStatus("PENDING");

        EmailNotificationDTO saved = notificationService.createNotification(dto);

        assertEquals("SENT", saved.getStatus());
        verify(mailService).sendSimpleEmail("patient@gmail.com", "Claim Update", "Your claim was approved successfully");
    }
}
