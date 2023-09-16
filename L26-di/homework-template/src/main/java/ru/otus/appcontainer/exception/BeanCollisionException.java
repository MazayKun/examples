package ru.otus.appcontainer.exception;

public class BeanCollisionException extends RuntimeException {
    public BeanCollisionException(Class<?> beanType) {
        super("There is two or more beans found for type " + beanType.getName());
    }

    public BeanCollisionException(String beanName) {
        super("There is two or more beans found with name " + beanName);
    }
}
