package com.ismolka.validation.utils.change.collection;

import com.ismolka.validation.utils.change.Difference;
import com.ismolka.validation.utils.change.value.ValueDifference;

import java.util.Map;
import java.util.Objects;

/**
 * Difference between two elements of {@link java.util.Collection}.
 *
 * @param diffBetweenElementsFields - difference between elements.
 * @param elementFromOldCollection - element from old collection.
 * @param elementFromNewCollection - element from new collection.
 * @param elementFromOldCollectionIndex - index of element from old collection.
 * @param elementFromNewCollectionIndex - index of element from new collection.
 * @param <F> - type of collection elements.
 *
 * @author Ihar Smolka
 */
public record CollectionElementDifference<F>(
        Map<String, Difference> diffBetweenElementsFields,
        F elementFromOldCollection,
        F elementFromNewCollection,
        Integer elementFromOldCollectionIndex,
        Integer elementFromNewCollectionIndex
) implements Difference {

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

    @Override
    public <TYPE extends Difference> TYPE unwrap(Class<TYPE> type) {
        if (type.isAssignableFrom(CollectionElementDifference.class)) {
            return type.cast(this);
        }

        throw new ClassCastException(String.format("Cannot unwrap CollectionElementDifference to %s", type));
    }
}
