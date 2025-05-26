package ru.javaprojects.picnest.common.mail;

import ru.javaprojects.picnest.common.error.LocalizedException;

public class MailException extends LocalizedException {
    public MailException(String message, String messageCode, Object[] messageArgs) {
        super(message, messageCode, messageArgs);
    }
}
