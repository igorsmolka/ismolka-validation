package com.ismolka.validation.utils.change.collection;

import com.ismolka.validation.utils.change.CheckerResult;
import com.ismolka.validation.utils.change.navigator.CheckerResultNavigator;
import com.ismolka.validation.utils.change.navigator.DefaultCheckerResultNavigator;
import com.ismolka.validation.utils.constant.CollectionOperation;
import com.ismolka.validation.utils.change.Difference;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Result for check two collections.
 *
 * @param collectionClass - collection value class.
 * @param collectionDifferenceMap - collection difference.
 * @param equalsResult - equals result
 * @param <F> - type of collection values
 *
 * @author Ihar Smolka
 */
public record CollectionChangesCheckerResult<F>(
        Class<F> collectionClass,
        Map<CollectionOperation, Set<CollectionElementDifference<F>>> collectionDifferenceMap,
        boolean equalsResult) implements Difference, CheckerResult {

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CollectionChangesCheckerResult<?> that = (CollectionChangesCheckerResult<?>) o;
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

    @Override
    public CheckerResultNavigator navigator() {
        return new DefaultCheckerResultNavigator(this);
    }
}
