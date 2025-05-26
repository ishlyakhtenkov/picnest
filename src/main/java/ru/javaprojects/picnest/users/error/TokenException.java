package ru.javaprojects.picnest.users.error;

import ru.javaprojects.picnest.common.error.LocalizedException;

public class TokenException extends LocalizedException {
    public TokenException(String message, String messageCode, Object[] messageArgs) {
        super(message, messageCode, messageArgs);
    }
}
