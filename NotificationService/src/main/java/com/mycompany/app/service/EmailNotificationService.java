package com.mycompany.app.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailNotificationService {

    // For testing purposes, we'll simulate email sending
    // In production, integrate with JavaMailSender or SendGrid
    public boolean sendEmail(String to, String subject, String text) {
        try {
            // Simulate email sending delay
            Thread.sleep(100);

            log.info("📧 EMAIL SENT to: {} | Subject: {} | Message: {}", to, subject, text);

            // In real implementation:
            // SimpleMailMessage message = new SimpleMailMessage();
            // message.setTo(to);
            // message.setSubject(subject);
            // message.setText(text);
            // mailSender.send(message);

            return true;

        } catch (Exception e) {
            log.error("Failed to send email to: {}", to, e);
            return false;
        }
    }
}
