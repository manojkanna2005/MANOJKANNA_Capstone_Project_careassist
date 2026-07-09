package com.hexaware.careassist.service.impl;

import lombok.extern.slf4j.Slf4j;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hexaware.careassist.dto.EmailNotificationDTO;
import com.hexaware.careassist.entity.AppUser;
import com.hexaware.careassist.entity.EmailNotification;
import com.hexaware.careassist.exception.ResourceNotFoundException;
import com.hexaware.careassist.repository.AppUserRepository;
import com.hexaware.careassist.repository.EmailNotificationRepository;
import com.hexaware.careassist.service.IEmailNotificationService;
import com.hexaware.careassist.service.IMailService;

@Service
@Transactional
@Slf4j
public class EmailNotificationServiceImpl implements IEmailNotificationService {

    @Autowired
    private EmailNotificationRepository notificationRepository;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private IMailService mailService;

    @Override
    public EmailNotificationDTO createNotification(EmailNotificationDTO dto) {
        EmailNotification notification = toEntity(dto);

        if (notification.getSentAt() == null) {
            notification.setSentAt(LocalDateTime.now());
        }
        if (notification.getStatus() == null || notification.getStatus().isBlank()) {
            notification.setStatus("PENDING");
        }

        EmailNotification saved = notificationRepository.save(notification);

        try {
            mailService.sendSimpleEmail(
                    saved.getAppUser().getEmail(),
                    saved.getSubject(),
                    saved.getMessage());
            saved.setStatus("SENT");
            log.info("Email notification sent notificationId={} userId={}", saved.getNotificatoinId(), saved.getAppUser().getUserId());
        } catch (Exception ex) {
            saved.setStatus("FAILED");
            log.error("Email notification failed notificationId={} userId={}", saved.getNotificatoinId(), saved.getAppUser().getUserId(), ex);
        }

        return toDTO(notificationRepository.save(saved));
    }

    @Override
    public EmailNotificationDTO getNotificationById(Integer notificationId) {
        return toDTO(getNotificationEntity(notificationId));
    }

    @Override
    public List<EmailNotificationDTO> getNotificationsByUserId(Integer userId) {
        return notificationRepository.findByAppUserUserId(userId).stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<EmailNotificationDTO> getAllNotifications() {
        return notificationRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public void deleteNotification(Integer notificationId) {
        notificationRepository.delete(getNotificationEntity(notificationId));
    }

    private EmailNotification getNotificationEntity(Integer notificationId) {
        return notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Email notification not found with id: " + notificationId));
    }

    private AppUser getUser(Integer userId) {
        return appUserRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
    }

    private EmailNotificationDTO toDTO(EmailNotification notification) {
        EmailNotificationDTO dto = new EmailNotificationDTO();
        dto.setNotificationId(notification.getNotificatoinId());
        dto.setUserId(notification.getAppUser().getUserId());
        dto.setSubject(notification.getSubject());
        dto.setMessage(notification.getMessage());
        dto.setSentAt(notification.getSentAt());
        dto.setStatus(notification.getStatus());
        return dto;
    }

    private EmailNotification toEntity(EmailNotificationDTO dto) {
        EmailNotification notification = new EmailNotification();
        notification.setNotificatoinId(dto.getNotificationId());
        notification.setAppUser(getUser(dto.getUserId()));
        notification.setSubject(dto.getSubject());
        notification.setMessage(dto.getMessage());
        notification.setSentAt(dto.getSentAt());
        notification.setStatus(dto.getStatus());
        return notification;
    }
}
