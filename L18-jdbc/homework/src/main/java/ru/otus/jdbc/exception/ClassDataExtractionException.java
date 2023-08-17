package ru.otus.jdbc.exception;

public class ClassDataExtractionException extends RuntimeException {

    public ClassDataExtractionException(String message) {
        super(message);
    }

    public ClassDataExtractionException(String message, Throwable cause) {
        super(message, cause);
    }
}
