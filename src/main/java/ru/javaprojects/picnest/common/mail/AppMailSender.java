package ru.javaprojects.picnest.common.mail;

import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
@Profile("!dev")
@RequiredArgsConstructor
@Slf4j
public class AppMailSender implements MailSender {
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String from;

    @Override
    public void sendEmail(String to, String subject, String text) {
        log.info("send email to {}:({})", to, subject);
        try {
            var message = mailSender.createMimeMessage();
            var mimeMessageHelper = new MimeMessageHelper(message, StandardCharsets.UTF_8.name());
            mimeMessageHelper.setFrom(from);
            mimeMessageHelper.setTo(to);
            mimeMessageHelper.setSubject(subject);
            mimeMessageHelper.setText(text, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new MailException("Failed to send mail (" + subject + "): " + e.getMessage(), "error.mail.failed-to-send",
                    new Object[]{subject});
        }
    }
}
