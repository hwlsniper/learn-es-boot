package com.learn.es.model;

import com.google.common.base.Objects;
import lombok.Data;

/**
 * 数据库和表
 */
@Data
public class DatabaseTableModel {
    private String database;
    private String table;

    public DatabaseTableModel() {
    }

    public DatabaseTableModel(String database, String table) {
        this.database = database;
        this.table = table;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DatabaseTableModel that = (DatabaseTableModel) o;
        return Objects.equal(database, that.database) &&
                Objects.equal(table, that.table);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(database, table);
    }
}
