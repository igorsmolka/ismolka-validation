package com.ismolka.validation.utils.change.map;

import com.ismolka.validation.utils.change.ChangesChecker;

import java.util.Map;

public interface MapChangesChecker<K, V> extends ChangesChecker<V> {

    MapChangesCheckerResult<K, V> getResult(Map<K, V> oldMap, Map<K, V> newMap);
}
