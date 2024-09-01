package com.ismolka.validation.utils.change.map;

import com.ismolka.validation.utils.change.ChangesChecker;

import java.util.Map;

/**
 * Interface for check differences between two maps.
 * @see MapChangesCheckerResult
 *
 * @param <K> - key type
 * @param <V> - value type
 *
 * @author Ihar Smolka
 */
public interface MapChangesChecker<K, V> extends ChangesChecker<V> {

    /**
     * Find difference between two maps.
     *
     * @param oldMap - old map
     * @param newMap - new map
     * @return difference result
     */
    MapChangesCheckerResult<K, V> getResult(Map<K, V> oldMap, Map<K, V> newMap);
}
