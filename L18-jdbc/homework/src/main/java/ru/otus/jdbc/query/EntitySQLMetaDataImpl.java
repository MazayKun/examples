package ru.otus.jdbc.query;

import ru.otus.jdbc.metadata.EntityClassMetaData;

import java.util.StringJoiner;

public class EntitySQLMetaDataImpl<T> implements EntitySQLMetaData<T> {

    private final String selectAll;
    private final String selectById;
    private final String insert;
    private final String updateById;
    private final EntityClassMetaData<T> entityClassMetaData;

    public EntitySQLMetaDataImpl(EntityClassMetaData<T> entityClassMetaData) {
        this.entityClassMetaData = entityClassMetaData;
        this.selectAll = "select * from " + entityClassMetaData.getName() + ";";
        this.selectById = "select * from "
                + entityClassMetaData.getName()
                + " where "
                + entityClassMetaData.getName() + '.' + entityClassMetaData.getIdField().getName()
                + " = ?;";
        StringJoiner valuesSequence = new StringJoiner(", ", "(", ");");
        for(var field : entityClassMetaData.getAllFields()) {
            valuesSequence.add(field.getName());
        }
        this.insert = "insert into " + entityClassMetaData.getName() + " values " + valuesSequence;
        StringJoiner setSequence = new StringJoiner(", ");
        for(var field : entityClassMetaData.getFieldsWithoutId()) {
            setSequence.add(entityClassMetaData.getName() + '.' + field.getName() + " = ?");
        }
        this.updateById = "update "
                + entityClassMetaData.getName()
                + " set " + setSequence
                + " where " + entityClassMetaData.getName() + '.' + entityClassMetaData.getIdField().getName() + " = ?;";
    }

    @Override
    public String getSelectAllSql() {
        return selectAll;
    }

    @Override
    public String getSelectByIdSql() {
        return selectById;
    }

    @Override
    public String getInsertSql() {
        return insert;
    }

    @Override
    public String getUpdateSql() {
        return updateById;
    }

    @Override
    public EntityClassMetaData<T> getEntityClassMetaData() {
        return entityClassMetaData;
    }
}
