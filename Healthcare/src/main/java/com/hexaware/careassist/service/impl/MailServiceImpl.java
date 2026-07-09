package com.hexaware.careassist.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.hexaware.careassist.service.IMailService;

import jakarta.mail.internet.MimeMessage;

@Service
@Slf4j
public class MailServiceImpl implements IMailService {

    @Autowired(required = false)
    private JavaMailSender javaMailSender;

    @Value("${app.mail.enabled:false}")
    private boolean mailEnabled;

    @Value("${app.mail.from:no-reply@careassist.local}")
    private String fromEmail;

    @Override
    public void sendSimpleEmail(String to, String subject, String body) {
        sendSimpleEmail(to, subject, body, null, null);
    }

    @Override
    public void sendSimpleEmail(String to, String subject, String body, String senderDisplayName, String replyToEmail) {
        if (!mailEnabled) {
            log.info("Email sending is disabled. Intended simple email to={} subject={} replyTo={}", to, subject, replyToEmail);
            return;
        }

        if (javaMailSender == null) {
            throw new IllegalStateException("JavaMailSender is not configured");
        }

        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            applySender(helper, senderDisplayName, replyToEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, false);
            javaMailSender.send(message);
            log.info("Simple email sent to={} subject={} replyTo={}", to, subject, replyToEmail);
        } catch (Exception ex) {
            log.error("Failed to send simple email to={} subject={} replyTo={}", to, subject, replyToEmail, ex);
            throw new IllegalStateException("Unable to send email. Please check SMTP configuration.");
        }
    }

    @Override
    public void sendEmailWithAttachment(String to, String subject, String body, String attachmentName, byte[] attachmentContent) {
        sendEmailWithAttachment(to, subject, body, attachmentName, attachmentContent, null, null);
    }

    @Override
    public void sendEmailWithAttachment(String to, String subject, String body, String attachmentName, byte[] attachmentContent,
            String senderDisplayName, String replyToEmail) {
        if (!mailEnabled) {
            log.info("Email sending is disabled. Intended attachment email to={} subject={} attachment={} replyTo={}",
                    to, subject, attachmentName, replyToEmail);
            return;
        }

        if (javaMailSender == null) {
            throw new IllegalStateException("JavaMailSender is not configured");
        }

        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            applySender(helper, senderDisplayName, replyToEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, false);
            helper.addAttachment(attachmentName, new ByteArrayResource(attachmentContent));
            javaMailSender.send(message);
            log.info("Attachment email sent to={} subject={} attachment={} replyTo={}", to, subject, attachmentName, replyToEmail);
        } catch (Exception ex) {
            log.error("Failed to send attachment email to={} subject={} attachment={} replyTo={}", to, subject, attachmentName, replyToEmail, ex);
            throw new IllegalStateException("Unable to send email with attachment. Please check SMTP configuration.");
        }
    }

    private void applySender(MimeMessageHelper helper, String senderDisplayName, String replyToEmail) throws Exception {
        if (senderDisplayName != null && !senderDisplayName.isBlank()) {
            helper.setFrom(fromEmail, senderDisplayName.trim());
        } else {
            helper.setFrom(fromEmail);
        }

        if (replyToEmail != null && !replyToEmail.isBlank()) {
            helper.setReplyTo(replyToEmail.trim());
        }
    }
}
