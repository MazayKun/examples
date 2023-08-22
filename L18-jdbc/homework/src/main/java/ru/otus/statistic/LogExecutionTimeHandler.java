package ru.otus.statistic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

public class LogExecutionTimeHandler implements InvocationHandler {

    private static final Logger log = LoggerFactory.getLogger(LogExecutionTimeHandler.class);

    private final Object originalObject;

    public LogExecutionTimeHandler(Object originalObject) {
        this.originalObject = originalObject;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
        Object result;
        long startTime = System.nanoTime();
        try {
             result = method.invoke(originalObject, args);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Error during invocation of " + method.getName() + " with arguments " + Arrays.toString(args));
        }
        long executionTime = System.nanoTime() - startTime;
        log.info(executionTime + "ns : " + method.getName() + " execution time");
        return result;
    }
}
