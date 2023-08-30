package ru.otus.appcontainer.exception;

public class NoArgsConstructorNotFoundException extends RuntimeException {

    public NoArgsConstructorNotFoundException(Class<?> clazz) {
        super("No args constructor required for class " + clazz.getName());
    }
}
