package ru.otus.appcontainer.exception;

public class NoBeanFoundException extends RuntimeException {

    public NoBeanFoundException(String beanName) {
        super("There is no bean with name " + beanName);
    }

    public NoBeanFoundException(Class<?> beanClass) {
        super("There is no bean with class " + beanClass.getName());
    }
}
