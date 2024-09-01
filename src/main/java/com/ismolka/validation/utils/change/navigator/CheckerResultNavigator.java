package com.ismolka.validation.utils.change.navigator;

import com.ismolka.validation.utils.change.Difference;
import com.ismolka.validation.utils.change.collection.CollectionElementDifference;
import com.ismolka.validation.utils.change.map.MapElementDifference;
import com.ismolka.validation.utils.constant.CollectionOperation;
import com.ismolka.validation.utils.constant.MapOperation;

import java.util.Set;

/**
 * Interface for navigation in {@link com.ismolka.validation.utils.change.CheckerResult}.
 * @see com.ismolka.validation.utils.change.CheckerResult
 *
 * @author Ihar Smolka
 */
public interface CheckerResultNavigator {

    /**
     * Get difference for {@link java.util.Map}
     *
     * @param fieldPath - attribute path with difference.
     * @param keyClass - key class.
     * @param valueClass - value class.
     * @param operations - return for {@link MapOperation}.
     * @return {@link Set} of {@link MapElementDifference} - if differences are there and 'null' - if aren't.
     * @param <K> - key type.
     * @param <V> - value type.
     */
    <K, V> Set<MapElementDifference<K, V>> getDifferenceForMap(String fieldPath, Class<K> keyClass, Class<V> valueClass, MapOperation... operations);

    /**
     * Get difference for {@link java.util.Collection}
     *
     * @param fieldPath - attribute path with difference.
     * @param forClass - class of collection values.
     * @param operations - return for {@link CollectionOperation}.
     * @return {@link Set} of {@link CollectionElementDifference} - if differences are there and 'null' - if aren't.
     * @param <T> - value type
     */
    <T> Set<CollectionElementDifference<T>> getDifferenceForCollection(String fieldPath, Class<T> forClass, CollectionOperation... operations);

    /**
     * Get difference for {@link java.util.Map}
     *
     * @param keyClass - key class.
     * @param valueClass - value class.
     * @param operations - return for {@link MapOperation}.
     * @return {@link Set} of {@link MapElementDifference} - if differences are there and 'null' - if aren't.
     * @param <K> - key type.
     * @param <V> - value type.
     */
    <K, V> Set<MapElementDifference<K, V>> getDifferenceForMap(Class<K> keyClass, Class<V> valueClass, MapOperation... operations);

    /**
     * Get difference for {@link java.util.Collection}
     *
     * @param forClass - class of collection values.
     * @param operations - return for {@link CollectionOperation}.
     * @return {@link Set} of {@link CollectionElementDifference} - if differences are there and 'null' - if aren't.
     * @param <T> - value type
     */
    <T> Set<CollectionElementDifference<T>> getDifferenceForCollection(Class<T> forClass, CollectionOperation... operations);

    /**
     * Get difference for attribute.
     *
     * @param fieldPath - attribute path with difference.
     * @return {@link Difference} - if differences are there and 'null' - if aren't.
     */
    Difference getDifference(String fieldPath);

    /**
     * Get difference.
     *
     * @return {@link Difference}
     */
    Difference getDifference();
}
