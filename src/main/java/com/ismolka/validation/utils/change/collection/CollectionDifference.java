package com.ismolka.validation.utils.change.collection;

import com.ismolka.validation.constraints.constant.CollectionOperation;
import com.ismolka.validation.utils.change.Difference;

import java.util.Map;
import java.util.Set;

public record CollectionDifference(Class<?> collectionGenericClass,
                                   Map<CollectionOperation, Set<CollectionElementDifference>> collectionDifference) implements Difference {

    @Override
    public <T extends Difference> T unwrap(Class<T> type) {
        if (type.isAssignableFrom(CollectionDifference.class)) {
            return type.cast(this);
        }

        throw new ClassCastException(String.format("Cannot unwrap CollectionDifference to %s", type));
    }
}
