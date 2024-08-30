package com.ismolka.validation.utils.change.map;

import com.ismolka.validation.utils.change.value.DefaultValueChangesChecker;
import com.ismolka.validation.utils.change.value.ValueChangesCheckerResult;
import com.ismolka.validation.utils.change.value.ValueCheckDescriptor;
import com.ismolka.validation.utils.constant.MapOperation;
import com.ismolka.validation.utils.metainfo.FieldPath;
import org.antlr.v4.runtime.misc.OrderedHashSet;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiPredicate;

public class DefaultMapChangesChecker<K, V> extends DefaultValueChangesChecker<V> implements MapChangesChecker<K, V> {

    private final Class<K> keyClass;

    private final Class<V> valueClass;

    private final Set<MapOperation> forOperations;

    public DefaultMapChangesChecker(Set<ValueCheckDescriptor<?>> attributesCheckDescriptors,
                                    boolean stopOnFirstDiff,
                                    Method globalEqualsMethodReflectionRef,
                                    BiPredicate<V, V> globalBiEqualsMethodCodeRef,
                                    Set<FieldPath> globalEqualsFields,
                                    Class<K> keyClass,
                                    Class<V> valueClass,
                                    Set<MapOperation> forOperations) {
        super(attributesCheckDescriptors, stopOnFirstDiff, globalEqualsMethodReflectionRef, globalBiEqualsMethodCodeRef, globalEqualsFields);
        this.keyClass = keyClass;
        this.valueClass = valueClass;
        this.forOperations = forOperations;
    }

    @Override
    public MapChangesCheckerResult<K, V> getResult(Map<K, V> oldMap, Map<K, V> newMap) {
        if (oldMap == newMap) {
            return new MapChangesCheckerResult<>(keyClass, valueClass, null, true);
        }

        if (oldMap == null || newMap == null) {
            return returnResultWhenOneIsNull(oldMap, newMap);
        }

        Map<MapOperation, Set<MapElementDifference<K, V>>> mapDifference = new HashMap<>();

        if (forOperations.contains(MapOperation.REMOVE) || forOperations.contains(MapOperation.UPDATE)) {
            for (Map.Entry<K, V> keyValueEntry : oldMap.entrySet()) {
                V newValue = newMap.get(keyValueEntry.getKey());
                V oldValue = keyValueEntry.getValue();

                if (newValue == null) {
                    if (forOperations.contains(MapOperation.REMOVE)) {
                        if (!mapDifference.containsKey(MapOperation.REMOVE)) {
                            mapDifference.put(MapOperation.REMOVE, new OrderedHashSet<>());
                        }

                        mapDifference.get(MapOperation.REMOVE).add(new MapElementDifference<>(null, oldValue, null, keyValueEntry.getKey()));
                        if (stopOnFirstDiff) {
                            break;
                        }
                    }
                } else if (forOperations.contains(MapOperation.UPDATE)) {
                    MapElementDifference<K, V> mapElementDifference = checkAndReturnDiff(oldValue, newValue, keyValueEntry.getKey());
                    if (mapElementDifference != null) {
                        if (!mapDifference.containsKey(MapOperation.UPDATE)) {
                            mapDifference.put(MapOperation.UPDATE, new OrderedHashSet<>());
                        }

                        mapDifference.get(MapOperation.UPDATE).add(mapElementDifference);
                        if (stopOnFirstDiff) {
                            break;
                        }
                    }
                }
            }
        }

        if (forOperations.contains(MapOperation.PUT)) {
            for (Map.Entry<K, V> keyValueEntry : newMap.entrySet()) {
                V oldValue = oldMap.get(keyValueEntry.getKey());
                V newValue = keyValueEntry.getValue();

                if (oldValue == null) {
                    if (!mapDifference.containsKey(MapOperation.PUT)) {
                        mapDifference.put(MapOperation.PUT, new OrderedHashSet<>());
                    }

                    mapDifference.get(MapOperation.PUT).add(new MapElementDifference<>(null, null, newValue, keyValueEntry.getKey()));
                    if (stopOnFirstDiff) {
                        break;
                    }
                }
            }
        }

        return new MapChangesCheckerResult<>(keyClass, valueClass, mapDifference, mapDifference.isEmpty());
    }

    private MapChangesCheckerResult<K, V> returnResultWhenOneIsNull(Map<K, V> oldMap, Map<K, V> newMap) {
        Set<MapElementDifference<K, V>> mapDifference = new OrderedHashSet<>();

        if (oldMap == null) {
            newMap.forEach((key, value) -> mapDifference.add(new MapElementDifference<>(null, null, value, key)));
        }

        if (newMap == null) {
            oldMap.forEach((key, value) -> mapDifference.add(new MapElementDifference<>(null, value, null, key)));
        }

        MapOperation operation = oldMap == null ? MapOperation.PUT : MapOperation.REMOVE;

        return new MapChangesCheckerResult<>(keyClass, valueClass, Map.of(operation, mapDifference), false);
    }

    private MapElementDifference<K, V> checkAndReturnDiff(V oldElement, V newElement, K key) {
        if (oldElement == newElement) {
            return null;
        }

        ValueChangesCheckerResult valueChangesCheckerResult = getResult(oldElement, newElement);
        if (!valueChangesCheckerResult.equalsResult()) {
            return new MapElementDifference<>(valueChangesCheckerResult.differenceMap(), oldElement, newElement, key);
        }

        return null;
    }
}
