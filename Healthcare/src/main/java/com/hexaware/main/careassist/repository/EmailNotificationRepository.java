package com.hexaware.main.careassist.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hexaware.main.careassist.entity.EmailNotification;


public interface EmailNotificationRepository extends JpaRepository<EmailNotification, Integer> {
    List<EmailNotification> findByAppUserUserId(Integer userId);
}
