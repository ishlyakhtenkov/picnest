package ru.javaprojects.picnest.users.error;

import ru.javaprojects.picnest.common.error.LocalizedException;

public class UserDisabledException extends LocalizedException {
    public UserDisabledException(String message, String messageCode, Object[] messageArgs) {
        super(message, messageCode, messageArgs);
    }
}

