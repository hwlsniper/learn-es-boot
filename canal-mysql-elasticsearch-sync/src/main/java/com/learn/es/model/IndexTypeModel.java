package com.learn.es.model;

import com.google.common.base.Objects;
import lombok.Data;

/**
 * 索引
 */
@Data
public class IndexTypeModel {
    private String index;

    public IndexTypeModel() {
    }

    public IndexTypeModel(String index) {
        this.index = index;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        IndexTypeModel that = (IndexTypeModel) o;
        return Objects.equal(index, that.index);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(index);
    }
}
