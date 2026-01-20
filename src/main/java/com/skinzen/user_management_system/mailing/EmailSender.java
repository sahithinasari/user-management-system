package com.skinzen.user_management_system.mailing;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class EmailSender {
    @Autowired
    private JavaMailSender mailSender;

    public void send(String to, String subject, String body) {
        log.info("Sending email to {}", to);
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper  = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }
}
