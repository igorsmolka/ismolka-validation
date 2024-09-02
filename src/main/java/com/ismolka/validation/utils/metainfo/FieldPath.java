package com.ismolka.validation.utils.metainfo;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public record FieldPath(
        Class<?> clazz,
        String path,
        List<FieldMetaInfo> pathFieldChain
) implements ClassFieldPathMetaInfo<FieldMetaInfo> {

    @Override
    public FieldMetaInfo getLast() {
        return pathFieldChain.get(pathFieldChain.size() - 1);
    }

    @Override
    public Object getValueFromObject(Object object) {
        Object result = object;

        for (FieldMetaInfo fieldFromChain : pathFieldChain) {
            result = fieldFromChain.getValueFromObject(result);
        }

        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FieldPath fieldPath = (FieldPath) o;
        return Objects.equals(clazz, fieldPath.clazz) && Objects.equals(path, fieldPath.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(clazz, path);
    }
}
