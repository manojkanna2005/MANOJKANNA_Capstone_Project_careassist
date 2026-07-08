package com.hexaware.main.careassist.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hexaware.main.careassist.dto.EmailNotificationDTO;
import com.hexaware.main.careassist.service.IEmailNotificationService;

import jakarta.validation.Valid;
@RestController
@RequestMapping("/api/v1/email-notifications")
public class EmailNotificationRestController {
	
	@Autowired
	private IEmailNotificationService emailNotificationService;

	@PostMapping("/add")
	public EmailNotificationDTO createNotification(@Valid @RequestBody EmailNotificationDTO dto) {
		return emailNotificationService.createNotification(dto);
	}

	@GetMapping("/{notificationId}")
	public EmailNotificationDTO getNotificationById(@PathVariable Integer notificationId) {
		return emailNotificationService.getNotificationById(notificationId);
	}

	@GetMapping("/user/{userId}")
	public List<EmailNotificationDTO> getNotificationsByUserId(@PathVariable Integer userId) {
		return emailNotificationService.getNotificationsByUserId(userId);
	}

	@GetMapping("/all")
	public List<EmailNotificationDTO> getAllNotifications() {
		return emailNotificationService.getAllNotifications();
	}

	@DeleteMapping("/delete/{notificationId}")
	public String deleteNotification(@PathVariable Integer notificationId) {
		emailNotificationService.deleteNotification(notificationId);
		return "Email notification deleted successfully";
	}
}
