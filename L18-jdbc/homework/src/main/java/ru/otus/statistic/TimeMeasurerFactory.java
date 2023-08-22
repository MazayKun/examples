package ru.otus.statistic;

import java.lang.reflect.Proxy;

public class TimeMeasurerFactory {

    @SuppressWarnings("unchecked")
    public static <T> T getInstance(T targetObject, Class<? super T> targetInterface) {
        return (T)Proxy.newProxyInstance(
                targetObject.getClass().getClassLoader(),
                new Class[]{targetInterface},
                new LogExecutionTimeHandler(targetObject)
        );
    }
}
