package com.pm.librarymanagementsystem.service.impl;

import com.pm.librarymanagementsystem.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender javaMailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromEmail;


    @Override
    @Async
    public void sendSubscriptionEmail(String to, String userName, String planName, LocalDateTime endDate) {
        sendEmailTemplate(to, "subscription-success", Map.of(
                "userName", userName,
                "planName", planName,
                "endDate", endDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        ), "¡Tu suscripción fue activada!");
    }

    @Override
    @Async
    public void sendPasswordResetEmail(String to, String userName, String resetLink) {
        sendEmailTemplate(to, "password-reset", Map.of(
                "userName", userName,
                "resetLink", resetLink
        ), "Restablecer tu contraseña");
    }

    private void sendEmailTemplate(String to, String templateName, Map<String, Object> variables, String subject) {
        try {
            Context context = new Context();
            context.setVariables(variables);
            String bodyHtml = templateEngine.process(templateName, context);

            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(bodyHtml, true);

            javaMailSender.send(message);

        } catch (MailException | MessagingException e) {
            log.error("Error enviando correo a {}: {}", to, e.getMessage(), e);
        }
    }
}
