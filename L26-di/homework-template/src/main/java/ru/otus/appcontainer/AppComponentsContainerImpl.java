package ru.otus.appcontainer;

import ru.otus.appcontainer.api.AppComponent;
import ru.otus.appcontainer.api.AppComponentsContainer;
import ru.otus.appcontainer.api.AppComponentsContainerConfig;
import ru.otus.appcontainer.api.PackageScan;
import ru.otus.appcontainer.exception.*;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

public class AppComponentsContainerImpl implements AppComponentsContainer {

    private static final String THIS_PACKAGE_SUFFIX;

    static {
        String packageName = AppComponentsContainerImpl.class.getPackageName();
        THIS_PACKAGE_SUFFIX = packageName.substring(packageName.lastIndexOf('.') + 1);
    }

    private final List<Object> appComponents = new ArrayList<>();
    private final Map<String, Object> appComponentsByName = new HashMap<>();

    public AppComponentsContainerImpl(Class<?>... initialClasses) {
        processBasePackage(
                getPackageNameForScan(initialClasses)
        );
    }

    public AppComponentsContainerImpl(String basePackageName) {
        processBasePackage(
                ContextInitInfo.singlePackageContext(basePackageName)
        );
    }

    @Override
    @SuppressWarnings("unchecked")
    public <C> C getAppComponent(Class<C> componentClass) {
        C result = (C) appComponentsByName.get(
                getBeanNameFromType(componentClass)
        );
        if (result == null) {
            for (Object appComponent : appComponents) {
                if (componentClass.isInstance(appComponent)) {
                    if (result == null) {
                        result = (C) appComponent;
                    } else {
                        throw new BeanCollisionException(componentClass);
                    }
                }
            }
        }
        if (result == null) {
            throw new NoBeanFoundException(componentClass);
        }
        return result;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <C> C getAppComponent(String componentName) {
        C component = (C) appComponentsByName.get(componentName);
        if (component == null) {
            throw new NoBeanFoundException(componentName);
        }
        return component;
    }

    private void processBasePackage(ContextInitInfo contextInitInfo) {
        try {
            Queue<Class<?>> potentialBeans = contextInitInfo.potentialBeans;
            if (potentialBeans == null) {
                potentialBeans = new LinkedList<>();
            }
            if (contextInitInfo.packagesToScan != null) {
                for (String packageToScan : contextInitInfo.packagesToScan) {
                    enrichPotentialBeansQueueWithAllAdjacentClasses(packageToScan, potentialBeans);
                }
            }
            enrichContextWithBeans(potentialBeans);
        } catch (URISyntaxException e) {
            throw new RuntimeException("Error during obtaining of package uri", e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Can't find class by name", e);
        }
    }

    private void enrichPotentialBeansQueueWithAllAdjacentClasses(String packageName, Queue<Class<?>> potentialBeans) throws URISyntaxException, ClassNotFoundException {

        Queue<String> packagesToProcess = new LinkedList<>();
        packagesToProcess.add(packageName);

        String currentPackageName;
        String currentFileName;
        while ((currentPackageName = packagesToProcess.poll()) != null) {

            URL currentPackageResource = ClassLoader.getSystemClassLoader()
                    .getResource(
                            currentPackageName.replace('.', '/')
                    );
            if (currentPackageResource == null) {
                throw new PackageNotFoundException(currentPackageName);
            }
            File currentPackage = new File(currentPackageResource.toURI());

            for (File file : currentPackage.listFiles()) {
                currentFileName = file.getName();
                if (currentFileName.endsWith(".class")) {
                    potentialBeans.add(
                            Class.forName(currentPackageName + '.' + currentFileName.substring(0, currentFileName.lastIndexOf(".")))
                    );
                } else {
                    if (currentFileName.equals(THIS_PACKAGE_SUFFIX)) continue;
                    packagesToProcess.add(currentPackageName + '.' + currentFileName);
                }
            }
        }
    }

    private void enrichContextWithBeans(Queue<Class<?>> potentialBeans) {
        List<OrderedNode<List<OrderedNode<Method>>>> factoryMethods = new ArrayList<>();
        Class<?> nextClass;
        List<OrderedNode<Method>> currConfigMethods;
        AppComponentsContainerConfig configAnnotation;
        while ((nextClass = potentialBeans.poll()) != null) {
            configAnnotation = nextClass.getAnnotation(AppComponentsContainerConfig.class);
            if (configAnnotation != null) {
                currConfigMethods = new ArrayList<>();
                enrichListWithFactoryMethodsFromConfig(nextClass, currConfigMethods);
                currConfigMethods.sort(OrderedNode::compareTo);
                factoryMethods.add(new OrderedNode<>(configAnnotation.order(), currConfigMethods));
            }
        }
        factoryMethods.sort(OrderedNode::compareTo);
        Method firstMethod;
        Object configInstance;
        for (OrderedNode<List<OrderedNode<Method>>> config : factoryMethods) {
            if (config.data.isEmpty()) {
                continue;
            }
            firstMethod = config.data.get(0).data;
            try {
                Constructor<?> noArgsConstructor = firstMethod.getDeclaringClass().getConstructor();
                configInstance = noArgsConstructor.newInstance();
            } catch (NoSuchMethodException e) {
                throw new NoArgsConstructorNotFoundException(firstMethod.getDeclaringClass());
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new BeanInstantiationException(firstMethod.getDeclaringClass());
            }
            for (OrderedNode<Method> factoryMethod : config.data) {
                try {
                    enrichContextWithSingleBean(factoryMethod.data, configInstance);
                } catch (InvocationTargetException | IllegalAccessException e) {
                    throw new BeanInstantiationException(factoryMethod.data.getReturnType());
                }
            }
        }
    }

    private void enrichContextWithSingleBean(Method factoryMethod, Object configInstance) throws InvocationTargetException, IllegalAccessException {
        Class<?>[] parametersTypes = factoryMethod.getParameterTypes();
        Parameter[] parametersInfo = factoryMethod.getParameters();
        if (parametersInfo.length != parametersTypes.length) {
            throw new RuntimeException("Factory method " + factoryMethod.getName()
                    + " has bad parameters info " + Arrays.toString(parametersInfo)
                    + ", " + Arrays.toString(parametersTypes)
            );
        }
        Object[] factoryMethodArgs = new Object[parametersInfo.length];
        Object currArg;
        for (int i = 0; i < factoryMethodArgs.length; i++) {
            currArg = appComponentsByName.get(parametersInfo[i].getName());
            if (currArg == null) {
                currArg = getAppComponent(parametersTypes[i]);
            }
            factoryMethodArgs[i] = currArg;
        }
        Object newBean = factoryMethod.invoke(configInstance, factoryMethodArgs);
        appComponents.add(newBean);
        Object result = appComponentsByName.put(
                factoryMethod.getAnnotation(AppComponent.class).name(),
                newBean
        );
        if (result != null) {
            throw new BeanCollisionException(factoryMethod.getAnnotation(AppComponent.class).name());
        }
    }

    private String getBeanNameFromType(Class<?> beanType) {
        char[] c = beanType.getSimpleName().toCharArray();
        c[0] = Character.toLowerCase(c[0]);
        return new String(c);
    }

    private void enrichListWithFactoryMethodsFromConfig(Class<?> configClass, List<OrderedNode<Method>> storage) {
        AppComponent appComponent;
        for (Method method : configClass.getMethods()) {
            appComponent = method.getAnnotation(AppComponent.class);
            if (appComponent != null) {
                storage.add(new OrderedNode<>(appComponent.order(), method));
            }
        }
    }

    private ContextInitInfo getPackageNameForScan(Class<?>... configClasses) {
        if (configClasses.length == 0) {
            throw new IllegalArgumentException("Required at least one config class");
        }
        AppComponentsContainerConfig configAnnotation;
        PackageScan packageScanAnnotation;
        Queue<Class<?>> potentialBeans = null;
        List<String> packagesToScan = null;
        for (Class<?> configClass : configClasses) {
            configAnnotation = configClass.getAnnotation(AppComponentsContainerConfig.class);
            packageScanAnnotation = configClass.getAnnotation(PackageScan.class);
            if (packageScanAnnotation == null && configAnnotation == null) {
                throw new IllegalArgumentException(String.format("Given class is not config %s", configClass.getName()));
            }
            if (packageScanAnnotation != null) {
                if (packagesToScan == null) packagesToScan = new ArrayList<>();
                if (packageScanAnnotation.path().equals("")) {
                    packagesToScan.add(configClass.getPackageName());
                    continue;
                }
                packagesToScan.add(packageScanAnnotation.path());
                continue;
            }
            if (potentialBeans == null) potentialBeans = new LinkedList<>();
            potentialBeans.add(configClass);
        }
        return new ContextInitInfo(potentialBeans, packagesToScan);
    }

    private static class ContextInitInfo {
        Queue<Class<?>> potentialBeans;
        List<String> packagesToScan;

        public ContextInitInfo(Queue<Class<?>> potentialBeans, List<String> packagesToScan) {
            this.potentialBeans = potentialBeans;
            this.packagesToScan = packagesToScan;
        }

        static ContextInitInfo singlePackageContext(String packageToScan) {
            return new ContextInitInfo(
                    null,
                    Collections.singletonList(packageToScan)
            );
        }
    }

    private static class OrderedNode<T> implements Comparable<OrderedNode<T>> {
        int order;
        T data;

        OrderedNode(int order, T data) {
            this.order = order;
            this.data = data;
        }

        @Override
        public int compareTo(OrderedNode<T> o) {
            return Integer.compare(this.order, o.order);
        }
    }
}
