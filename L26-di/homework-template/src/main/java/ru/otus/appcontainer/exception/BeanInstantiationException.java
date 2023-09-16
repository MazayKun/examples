package ru.otus.appcontainer.exception;

public class BeanInstantiationException extends RuntimeException {

    public BeanInstantiationException(Class<?> beanType) {
        super("Cannot create instance of bean " + beanType.getName());
    }
}
