package com.ismolka.validation.utils.change.collection;

import com.ismolka.validation.utils.change.Difference;

import java.util.Map;
import java.util.Objects;

public record CollectionElementDifference<F>(
        Map<String, Difference> diffBetweenElementsFields,
        F elementFromOldCollection,
        F elementFromNewCollection,
        Integer elementFromOldCollectionIndex,
        Integer elementFromNewCollectionIndex
) {

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CollectionElementDifference<?> that = (CollectionElementDifference<?>) o;
        return Objects.equals(diffBetweenElementsFields, that.diffBetweenElementsFields) && Objects.equals(elementFromOldCollection, that.elementFromOldCollection) && Objects.equals(elementFromNewCollection, that.elementFromNewCollection) && Objects.equals(elementFromOldCollectionIndex, that.elementFromOldCollectionIndex) && Objects.equals(elementFromNewCollectionIndex, that.elementFromNewCollectionIndex);
    }

    @Override
    public int hashCode() {
        return Objects.hash(diffBetweenElementsFields, elementFromOldCollection, elementFromNewCollection, elementFromOldCollectionIndex, elementFromNewCollectionIndex);
    }
}
