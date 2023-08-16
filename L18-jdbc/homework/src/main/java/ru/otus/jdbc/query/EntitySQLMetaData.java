package ru.otus.jdbc.query;

import ru.otus.jdbc.metadata.EntityClassMetaData;

/**
 * Создает SQL - запросы
 */
public interface EntitySQLMetaData<T> {
    String getSelectAllSql();

    String getSelectByIdSql();

    String getInsertSql();

    String getUpdateSql();

    EntityClassMetaData<T> getEntityClassMetaData();
}
