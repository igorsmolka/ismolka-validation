package com.ismolka.validation.utils.change.collection;

import com.ismolka.validation.utils.change.ChangesChecker;

import java.util.Collection;

/**
 * Interface for check differences between two collections.
 * @see CollectionChangesCheckerResult
 *
 * @param <T> - collection value type
 *
 * @author Ihar Smolka
 */
public interface CollectionChangesChecker<T> extends ChangesChecker<T> {

    /**
     * Find difference between two collections.
     *
     * @param oldCollection - old collection
     * @param newCollection - new collection
     * @return {@link CollectionChangesCheckerResult}
     */
    CollectionChangesCheckerResult<T> getResult(Collection<T> oldCollection, Collection<T> newCollection);

    /**
     * Find difference between two arrays
     *
     * @param oldArray - old array
     * @param newArray - new array
     * @return {@link CollectionChangesCheckerResult}
     */
    CollectionChangesCheckerResult<T> getResult(T[] oldArray, T[] newArray);
}
