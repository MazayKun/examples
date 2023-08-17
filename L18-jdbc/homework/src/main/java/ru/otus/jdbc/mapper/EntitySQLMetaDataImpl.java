package ru.otus.jdbc.mapper;

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
        StringJoiner fieldNamesSequence = new StringJoiner(", ", "(", ")");
        StringJoiner valuesSequence = new StringJoiner(", ", "(", ");");
        for(var field : entityClassMetaData.getFieldsWithoutId()) {
            fieldNamesSequence.add(field.getName());
            valuesSequence.add("?");
        }
        this.insert = "insert into " + entityClassMetaData.getName() + fieldNamesSequence + " values " + valuesSequence;
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
