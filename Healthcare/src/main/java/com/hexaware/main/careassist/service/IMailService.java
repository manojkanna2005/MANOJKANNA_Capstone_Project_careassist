package com.hexaware.main.careassist.service;

public interface IMailService {
    void sendSimpleEmail(String to, String subject, String body);
    void sendSimpleEmail(String to, String subject, String body, String senderDisplayName, String replyToEmail);
    void sendEmailWithAttachment(String to, String subject, String body, String attachmentName, byte[] attachmentContent);
    void sendEmailWithAttachment(String to, String subject, String body, String attachmentName, byte[] attachmentContent,
            String senderDisplayName, String replyToEmail);
}
