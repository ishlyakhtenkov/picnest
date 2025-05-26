package ru.javaprojects.picnest.common.mail;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
@Slf4j
public class MockMailSender implements MailSender{
    @Override
    public void sendEmail(String to, String subject, String text) {
        log.info("send email to {}:({})", to, subject);
    }
}
