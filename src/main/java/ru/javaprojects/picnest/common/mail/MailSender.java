package ru.javaprojects.picnest.common.mail;

public interface MailSender {
    void sendEmail(String to, String subject, String text);
}

