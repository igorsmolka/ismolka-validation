package com.ismolka.validation.utils.metainfo;

import java.util.List;
import java.util.Optional;

public record DatabaseFieldPath(
        Class<?> clazz,
        String path,
        List<DatabaseFieldMetaInfo> pathFieldChain
) implements ClassFieldPathMetaInfo<DatabaseFieldMetaInfo> {

    @Override
    public DatabaseFieldMetaInfo getLast() {
        return pathFieldChain.get(pathFieldChain.size() - 1);
    }

    @Override
    public Object getValueFromObject(Object object) {
        Object result = object;

        for (DatabaseFieldMetaInfo fieldFromChain : pathFieldChain) {
            result = fieldFromChain.getValueFromObject(result);
        }

        return result;
    }

    public boolean isIdentifierPath() {
        return pathFieldChain.stream().anyMatch(fieldMetaInfo -> fieldMetaInfo.simpleId() || fieldMetaInfo.embeddedId());
    }

    public DatabaseFieldMetaInfo findFirstJoin() {
        return pathFieldChain.stream().filter(DatabaseFieldMetaInfo::join).findFirst().orElse(null);
    }

    public boolean needsJoin() {
        Optional<DatabaseFieldMetaInfo> fieldInChainWithJoin = pathFieldChain.stream().filter(DatabaseFieldMetaInfo::join).findFirst();
        return fieldInChainWithJoin.isPresent();
    }
}
