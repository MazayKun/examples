package ru.otus.jdbc.exception;

import java.lang.reflect.Field;

public class EntityDataExtractionException extends RuntimeException {
    public EntityDataExtractionException(Field field, Object entity, Throwable cause) {
        super("Error during extraction of " + field.getName() + " value from instance of " + field.getDeclaringClass() + '(' + entity + ')', cause);
    }

    public EntityDataExtractionException(Field field, Throwable cause) {
        super("Error during extraction of " + field.getName() + " value for instance of " + field.getDeclaringClass(), cause);
    }
}
