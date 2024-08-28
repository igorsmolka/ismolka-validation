package com.ismolka.validation.utils.change.collection;

import com.ismolka.validation.constraints.constant.CollectionOperation;
import com.ismolka.validation.utils.change.Difference;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

public record CollectionChangesCheckerResult(Map<CollectionOperation, Set<CollectionElementDifference>> collectionDifferenceMap,
                                             boolean equalsResult) implements Difference {

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CollectionChangesCheckerResult that = (CollectionChangesCheckerResult) o;
        return equalsResult == that.equalsResult && Objects.equals(collectionDifferenceMap, that.collectionDifferenceMap);
    }

    @Override
    public int hashCode() {
        return Objects.hash(collectionDifferenceMap, equalsResult);
    }

    @Override
    public <T extends Difference> T unwrap(Class<T> type) {
        if (type.isAssignableFrom(CollectionChangesCheckerResult.class)) {
            return type.cast(this);
        }

        throw new ClassCastException(String.format("Cannot unwrap CollectionChangesCheckerResult to %s", type));
    }
}
