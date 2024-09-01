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
     * @return this
     */
    CollectionChangesCheckerResult<T> getResult(Collection<T> oldCollection, Collection<T> newCollection);
}
