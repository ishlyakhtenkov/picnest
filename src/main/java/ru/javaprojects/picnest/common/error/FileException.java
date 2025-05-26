package ru.javaprojects.picnest.common.error;

public class FileException extends LocalizedException {
    public FileException(String message, String messageCode, Object[] messageArgs) {
        super(message, messageCode, messageArgs);
    }
}
