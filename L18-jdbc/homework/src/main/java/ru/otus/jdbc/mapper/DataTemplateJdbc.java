package ru.otus.jdbc.mapper;

import ru.otus.core.repository.DataTemplate;
import ru.otus.core.repository.executor.DbExecutor;
import ru.otus.jdbc.exception.EntityDataExtractionException;
import ru.otus.jdbc.exception.EntityInstantiationException;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * Сохратяет объект в базу, читает объект из базы
 */
@SuppressWarnings("unchcked")
public class DataTemplateJdbc<T> implements DataTemplate<T> {

    private final DbExecutor dbExecutor;
    private final EntitySQLMetaData<T> entitySQLMetaData;
    private final Function<ResultSet, T> mapper;

    public DataTemplateJdbc(DbExecutor dbExecutor, EntitySQLMetaData<T> entitySQLMetaData) {
        this.dbExecutor = dbExecutor;
        this.entitySQLMetaData = entitySQLMetaData;
        this.mapper = rs -> {
            List<Field> allFields = entitySQLMetaData.getEntityClassMetaData().getAllFields();
            Object[] fieldsValues = new Object[allFields.size()];
            for (int i = 0; i < allFields.size(); i++) {
                try {
                    fieldsValues[i] = rs.getObject(i + 1);
                } catch (SQLException e) {
                    throw new EntityDataExtractionException(allFields.get(i), e);
                }
            }
            try {
                return entitySQLMetaData.getEntityClassMetaData().getConstructor().newInstance(fieldsValues);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new EntityInstantiationException(entitySQLMetaData.getEntityClassMetaData().getConstructor().getDeclaringClass(), e);
            }
        };
    }

    @Override
    public Optional<T> findById(Connection connection, long id) {
        return dbExecutor.executeSelect(
                connection,
                entitySQLMetaData.getSelectByIdSql(),
                List.of(id),
                mapper);
    }

    @Override
    public List<T> findAll(Connection connection) {
        return dbExecutor.executeMultiSelect(
                connection,
                entitySQLMetaData.getSelectAllSql(),
                Collections.emptyList(),
                mapper);
    }

    @Override
    public long insert(Connection connection, T entity) {
        return dbExecutor.executeStatement(
                connection,
                entitySQLMetaData.getInsertSql(),
                extractFieldsWithoutId(entity));
    }

    @Override
    public void update(Connection connection, T entity) {
        Object idFieldValue;
        try {
            idFieldValue = entitySQLMetaData.getEntityClassMetaData().getIdField().get(entity);
        } catch (IllegalAccessException e) {
            throw new EntityDataExtractionException(entitySQLMetaData.getEntityClassMetaData().getIdField(), entity, e);
        }
        List<Object> fieldsValues = extractFieldsWithoutId(entity);
        fieldsValues.add(0, idFieldValue);
        dbExecutor.executeStatement(
                connection,
                entitySQLMetaData.getUpdateSql(),
                fieldsValues);
    }

    private List<Object> extractFieldsWithoutId(T entity) {
        List<Field> fields = entitySQLMetaData.getEntityClassMetaData().getFieldsWithoutId();
        List<Object> fieldsValues = new ArrayList<>(fields.size() + 1);
        for (var field : fields) {
            try {
                fieldsValues.add(field.get(entity));
            } catch (IllegalAccessException e) {
                throw new EntityDataExtractionException(field, entity, e);
            }
        }
        return fieldsValues;
    }
}
