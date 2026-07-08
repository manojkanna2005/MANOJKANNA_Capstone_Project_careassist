package com.hexaware.main.careassist.service;

import java.util.List;
import com.hexaware.main.careassist.dto.EmailNotificationDTO;

public interface IEmailNotificationService {
    EmailNotificationDTO createNotification(EmailNotificationDTO dto);
    EmailNotificationDTO getNotificationById(Integer notificationId);
    List<EmailNotificationDTO> getNotificationsByUserId(Integer userId);
    List<EmailNotificationDTO> getAllNotifications();
    void deleteNotification(Integer notificationId);
}
