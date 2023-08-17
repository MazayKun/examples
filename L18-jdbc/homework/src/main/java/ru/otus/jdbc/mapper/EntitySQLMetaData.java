package ru.otus.jdbc.mapper;

import ru.otus.jdbc.mapper.EntityClassMetaData;

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
