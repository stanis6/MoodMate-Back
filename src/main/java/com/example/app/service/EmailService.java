package com.example.app.service;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;

    public void sendHtmlEmail(String to, String subject, String htmlBody) {
        MimeMessage msg = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(msg, "utf-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);  // note the `true` for HTML
            mailSender.send(msg);
        } catch (MessagingException e) {
            throw new MailException("Failed to send HTML email", e) {};
        }
    }

    public void sendEmailWithAttachment(
            String to,
            String subject,
            String body,
            String attachmentFilename,
            byte[] attachmentBytes
    ) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, false);

            ByteArrayResource pdfResource = new ByteArrayResource(attachmentBytes);
            helper.addAttachment(attachmentFilename, pdfResource);

            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send email with attachment", e);
        }
    }
}