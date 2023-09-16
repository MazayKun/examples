package ru.otus.appcontainer.exception;

public class PackageNotFoundException extends RuntimeException {
    public PackageNotFoundException(String packageName) {
        super("Package with name " + packageName + " not found");
    }
}
