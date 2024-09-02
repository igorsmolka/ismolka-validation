package com.ismolka.validation.utils.metainfo;

import org.springframework.util.ReflectionUtils;

import java.util.Objects;

public record DatabaseFieldMetaInfo(
        FieldMetaInfo field,
        boolean embeddedId,
        boolean simpleId,
        boolean join
) implements ClassFieldMetaInfo {

    @Override
    public Object getValueFromObject(Object obj) {
        return field.getValueFromObject(obj);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DatabaseFieldMetaInfo that = (DatabaseFieldMetaInfo) o;
        return embeddedId == that.embeddedId && simpleId == that.simpleId && join == that.join && Objects.equals(field, that.field);
    }

    @Override
    public int hashCode() {
        return Objects.hash(field, embeddedId, simpleId, join);
    }
}
