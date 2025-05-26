package ru.javaprojects.picnest.common.error;

import lombok.Getter;

@Getter
public abstract class LocalizedException extends RuntimeException {
    private final String messageCode;
    private final Object[] messageArgs;

    public LocalizedException(String message, String messageCode, Object[] messageArgs) {
        super(message);
        this.messageCode = messageCode;
        this.messageArgs = messageArgs;
    }
}
