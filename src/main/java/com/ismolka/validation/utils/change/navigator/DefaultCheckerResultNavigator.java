package com.ismolka.validation.utils.change.navigator;

import com.ismolka.validation.utils.change.CheckerResult;
import com.ismolka.validation.utils.change.Difference;
import com.ismolka.validation.utils.change.collection.CollectionChangesCheckerResult;
import com.ismolka.validation.utils.change.collection.CollectionElementDifference;
import com.ismolka.validation.utils.change.map.MapChangesCheckerResult;
import com.ismolka.validation.utils.change.map.MapElementDifference;
import com.ismolka.validation.utils.change.value.DifferenceRef;
import com.ismolka.validation.utils.change.value.ValueChangesCheckerResult;
import com.ismolka.validation.utils.change.value.ValueDifference;
import com.ismolka.validation.utils.constant.CollectionOperation;
import com.ismolka.validation.utils.constant.MapOperation;
import io.micrometer.common.util.StringUtils;
import org.antlr.v4.runtime.misc.OrderedHashSet;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class DefaultCheckerResultNavigator implements CheckerResultNavigator {

    private final CheckerResult checkerResult;

    public DefaultCheckerResultNavigator(CheckerResult checkerResult) {
        this.checkerResult = checkerResult;
    }


    @Override
    public <K, V> Set<MapElementDifference<K, V>> getDifferenceForMap(String fieldPath, Class<K> keyClass, Class<V> valueClass, MapOperation... operations) {
        if (checkerResult.equalsResult()) {
            return null;
        }

        MapOperation[] forOperations;

        if (operations == null || operations.length == 0) {
            forOperations = MapOperation.values();
        } else {
            forOperations = operations;
        }

        Difference extractedDifference = getDifference(fieldPath, checkerResult);

        if (extractedDifference == null) {
            return null;
        }

        if (!MapChangesCheckerResult.class.isAssignableFrom(extractedDifference.getClass())) {
            throw new IllegalArgumentException("Difference is not a map");
        }

        MapChangesCheckerResult<?, ?> mapChangesCheckerResult = extractedDifference.unwrap(MapChangesCheckerResult.class);

        if (!mapChangesCheckerResult.keyClass().equals(keyClass)) {
            throw new IllegalArgumentException(String.format("Expected key class: %s, provided class: %s", keyClass, valueClass));
        }
        if (!mapChangesCheckerResult.valueClass().equals(valueClass)) {
            throw new IllegalArgumentException(String.format("Expected value class: %s, provided class: %s", keyClass, valueClass));
        }

        MapChangesCheckerResult<K, V> castedMapChangesCheckerResult = (MapChangesCheckerResult<K, V>) mapChangesCheckerResult;
        Set<MapElementDifference<K, V>> resultSet = new OrderedHashSet<>();

        for (MapOperation operation : forOperations) {
            Set<MapElementDifference<K, V>> extractedMapDifferenceSet = castedMapChangesCheckerResult.mapDifference().get(operation);

            if (extractedMapDifferenceSet != null) {
                resultSet.addAll(extractedMapDifferenceSet);
            }
        }

        return resultSet;
    }

    @Override
    public <T> Set<CollectionElementDifference<T>> getDifferenceForCollection(String fieldPath, Class<T> forClass, CollectionOperation... operations) {
        if (checkerResult.equalsResult()) {
            return null;
        }

        CollectionOperation[] forOperations;

        if (operations == null || operations.length == 0) {
            forOperations = CollectionOperation.values();
        } else {
            forOperations = operations;
        }

        Difference extractedDifference = getDifference(fieldPath, checkerResult);

        if (extractedDifference == null) {
            return null;
        }

        if (!CollectionChangesCheckerResult.class.isAssignableFrom(extractedDifference.getClass())) {
            throw new IllegalArgumentException("Difference is not a collection");
        }

        CollectionChangesCheckerResult<?> collectionChangesCheckerResult = extractedDifference.unwrap(CollectionChangesCheckerResult.class);
        if (!collectionChangesCheckerResult.collectionClass().equals(forClass)) {
            throw new IllegalArgumentException(String.format("Expected collection class: %s, provided class: %s", forClass, extractedDifference.getClass()));
        }

        CollectionChangesCheckerResult<T> castedCollectionChangesCheckerResult = (CollectionChangesCheckerResult<T>) collectionChangesCheckerResult;
        Set<CollectionElementDifference<T>> resultSet = new OrderedHashSet<>();

        for (CollectionOperation operation : forOperations) {
            Set<CollectionElementDifference<T>> extractedCollectionDifferenceSet = castedCollectionChangesCheckerResult.collectionDifferenceMap().get(operation);

            if (extractedCollectionDifferenceSet != null) {
                resultSet.addAll(extractedCollectionDifferenceSet);
            }
        }

        return resultSet;
    }

    @Override
    public Difference getDifference(String fieldPath) {
        if (checkerResult.equalsResult()) {
            return null;
        }

        return getDifference(fieldPath, checkerResult);
    }

    @Override
    public Difference getDifference() {
        if (checkerResult.equalsResult()) {
            return null;
        }

        return getDifference(null, checkerResult);
    }

    @Override
    public <T> Set<CollectionElementDifference<T>> getDifferenceForCollection(Class<T> forClass, CollectionOperation... operations) {
        return getDifferenceForCollection(null, forClass, operations);
    }

    @Override
    public <K, V> Set<MapElementDifference<K, V>> getDifferenceForMap(Class<K> keyClass, Class<V> valueClass, MapOperation... operations) {
        return getDifferenceForMap(null, keyClass, valueClass, operations);
    }


    private Difference getDifference(String fieldPath, Difference difference) {
        if (StringUtils.isBlank(fieldPath)) {
            return getDifference(difference);
        }

        String[] fields = fieldPath.split("\\.");

        Difference lastDiff = difference;
        int currentFieldIndex = 0;
        boolean endIsReached = false;

        do {
            String field = fields[currentFieldIndex];
            Class<?> lastDiffClass = lastDiff.getClass();

            if (ValueDifference.class.isAssignableFrom(lastDiffClass)) {
                if (currentFieldIndex != fields.length - 1) {
                    return null;
                }

                return lastDiff;
            }

            if (CollectionChangesCheckerResult.class.isAssignableFrom(lastDiffClass) || MapChangesCheckerResult.class.isAssignableFrom(lastDiffClass)) {
                if (currentFieldIndex != fields.length - 1) {
                    throw new RuntimeException(String.format("Cannot extract result: unexpected collection or map on the path %s", field));
                } else {
                    return lastDiff;
                }
            }

            if (ValueChangesCheckerResult.class.isAssignableFrom(lastDiffClass)) {
                ValueChangesCheckerResult valueChangesCheckerResult = lastDiff.unwrap(ValueChangesCheckerResult.class);

                if (!valueChangesCheckerResult.differenceMap().containsKey(field) && !valueChangesCheckerResult.differenceMap().containsKey(fieldPath)) {
                    return null;
                }

                if (valueChangesCheckerResult.differenceMap().containsKey(fieldPath)) {
                    lastDiff = valueChangesCheckerResult.differenceMap().get(fieldPath);
                    continue;
                }

                lastDiff = valueChangesCheckerResult.differenceMap().get(field);
                lastDiffClass = lastDiff.getClass();
                currentFieldIndex++;

                if (currentFieldIndex >= fields.length) {
                    return lastDiff;
                }

                field = fields[currentFieldIndex];
            }

            if (DifferenceRef.class.isAssignableFrom(lastDiffClass)) {
                DifferenceRef differenceRef = lastDiff.unwrap(DifferenceRef.class);

                if (!Objects.equals(differenceRef.onField(), field)) {
                    return null;
                }

                lastDiff = differenceRef.toDifference();
                currentFieldIndex++;

                if (currentFieldIndex >= fields.length) {
                    return lastDiff;
                }
                continue;
            }

            endIsReached = true;
        } while (!endIsReached);

        return null;
    }

    private Difference getDifference(Difference difference) {
        Class<?> diffClass = difference.getClass();

        if (ValueChangesCheckerResult.class.isAssignableFrom(diffClass)) {
            return difference.unwrap(ValueChangesCheckerResult.class).differenceMap().get(null);
        }

        if (DifferenceRef.class.isAssignableFrom(diffClass)) {
            throw new RuntimeException("Cannot extract result: unexpected reference");
        }

        return difference;
    }
}
