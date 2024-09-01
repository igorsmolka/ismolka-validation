package com.ismolka.validation.utils.change;

import com.ismolka.validation.utils.change.value.ValueChangesCheckerResult;

/**
 * Interface for finding differences between two objects.
 * @param <T> - type of objects
 * @see ValueChangesCheckerResult
 *
 * @author Ihar Smolka
 */
public interface ChangesChecker<T> {

    /**
     * Find differences between two objects.
     * @param oldObj - old object
     * @param newObj - new object
     * @return finding result
     */
    ValueChangesCheckerResult getResult(T oldObj, T newObj);
}
