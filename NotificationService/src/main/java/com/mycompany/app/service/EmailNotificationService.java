package com.mycompany.app.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailNotificationService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.from:noreply@mycompany.com}")
    private String fromEmail;

    public boolean sendEmail(String to, String subject, String text) {
        try {

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);

            mailSender.send(message);
            return true;

        } catch (Exception e) {
            return false;
        }
    }

    public boolean sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            return true;

        } catch (Exception e) {
            return false;
        }
    }
}