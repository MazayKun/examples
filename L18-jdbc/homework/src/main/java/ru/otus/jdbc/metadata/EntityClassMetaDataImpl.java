package ru.otus.jdbc.metadata;

import ru.otus.core.annotation.Id;
import ru.otus.crm.model.Client;

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

    public EntityClassMetaDataImpl(Class<T> className) {
        tableName = className.getSimpleName().toLowerCase(Locale.ROOT);
        Field[] fieldsArray = className.getDeclaredFields();
        Class<?>[] allFieldsTypes = new Class[fieldsArray.length];
        this.allFields = new ArrayList<>(fieldsArray.length);
        this.fieldsWithoutId = new ArrayList<>(fieldsArray.length - 1);
        for(int i = 0; i < fieldsArray.length; i++) {
            allFieldsTypes[i] = fieldsArray[i].getDeclaringClass();
            allFields.add(fieldsArray[i]);
            if(fieldsArray[i].isAnnotationPresent(Id.class)) {
                this.idField = fieldsArray[i];
                continue;
            }
            fieldsWithoutId.add(fieldsArray[i]);
        }
        if(this.idField == null) throw new RuntimeException("Error during id field search");
        try {
            this.constructor = className.getConstructor(allFieldsTypes);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Error during constructor search", e);
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
