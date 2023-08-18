package ru.otus.jdbc.mapper;

import ru.otus.core.annotation.Id;
import ru.otus.jdbc.exception.ClassDataExtractionException;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class EntityClassMetaDataImpl<T> implements EntityClassMetaData<T> {

    private final String tableName;
    private final Constructor<T> constructor;
    private final List<Field> allFields;
    private final List<Field> fieldsWithoutId;
    private Field idField;

    public EntityClassMetaDataImpl(Class<T> clazz) {
        tableName = clazz.getSimpleName().toLowerCase(Locale.ROOT);
        Field[] fieldsArray = clazz.getDeclaredFields();
        Class<?>[] allFieldsTypes = new Class[fieldsArray.length];
        this.allFields = new ArrayList<>(fieldsArray.length);
        this.fieldsWithoutId = new ArrayList<>(fieldsArray.length - 1);
        boolean idFound = false;
        for (int i = 0; i < fieldsArray.length; i++) {
            fieldsArray[i].setAccessible(true);
            allFieldsTypes[i] = fieldsArray[i].getType();
            allFields.add(fieldsArray[i]);
            if (fieldsArray[i].isAnnotationPresent(Id.class)) {
                if(idFound) throw new ClassDataExtractionException("Two id fields found for entity " + clazz);
                idFound = true;
                this.idField = fieldsArray[i];
                continue;
            }
            fieldsWithoutId.add(fieldsArray[i]);
        }
        if (this.idField == null) throw new ClassDataExtractionException("Error during id field search");
        try {
            this.constructor = clazz.getConstructor(allFieldsTypes);
        } catch (NoSuchMethodException e) {
            throw new ClassDataExtractionException("Error during constructor search", e);
        }
    }

    @Override
    public String getName() {
        return tableName;
    }

    @Override
    public Constructor<T> getConstructor() {
        return constructor;
    }

    @Override
    public Field getIdField() {
        return idField;
    }

    @Override
    public List<Field> getAllFields() {
        return allFields;
    }

    @Override
    public List<Field> getFieldsWithoutId() {
        return fieldsWithoutId;
    }
}
