package com.ismolka.validation.utils.change.navigator;

import com.ismolka.validation.utils.change.Difference;
import com.ismolka.validation.utils.change.collection.CollectionElementDifference;
import com.ismolka.validation.utils.change.map.MapElementDifference;
import com.ismolka.validation.utils.constant.CollectionOperation;
import com.ismolka.validation.utils.constant.MapOperation;

import java.util.Set;

public interface CheckerResultNavigator {

    <K, V> Set<MapElementDifference<K, V>> getDifferenceForMap(String fieldPath, Class<K> keyClass, Class<V> valueClass, MapOperation... operations);

    <T> Set<CollectionElementDifference<T>> getDifferenceForCollection(String fieldPath, Class<T> forClass, CollectionOperation... operations);

    <K, V> Set<MapElementDifference<K, V>> getDifferenceForMap(Class<K> keyClass, Class<V> valueClass, MapOperation... operations);

    <T> Set<CollectionElementDifference<T>> getDifferenceForCollection(Class<T> forClass, CollectionOperation... operations);

    Difference getDifference(String fieldPath);

    Difference getDifference();
}
