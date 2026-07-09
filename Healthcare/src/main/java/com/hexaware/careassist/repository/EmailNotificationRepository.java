package com.hexaware.careassist.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hexaware.careassist.entity.EmailNotification;


public interface EmailNotificationRepository extends JpaRepository<EmailNotification, Integer> {
    List<EmailNotification> findByAppUserUserId(Integer userId);
}
