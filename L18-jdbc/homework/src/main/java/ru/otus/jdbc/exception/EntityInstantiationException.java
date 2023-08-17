package ru.otus.jdbc.exception;

public class EntityInstantiationException extends RuntimeException {
    public EntityInstantiationException(Class<?> entityClass, Throwable cause) {
        super("Error during constructing new instance of " + entityClass, cause);
    }
}
