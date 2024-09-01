package com.ismolka.validation.utils.change.navigator;

import com.ismolka.validation.utils.change.CheckerResult;
import com.ismolka.validation.utils.change.Difference;
import com.ismolka.validation.utils.change.collection.CollectionChangesCheckerResult;
import com.ismolka.validation.utils.change.collection.CollectionElementDifference;
import com.ismolka.validation.utils.change.map.MapChangesCheckerResult;
import com.ismolka.validation.utils.change.value.DifferenceRef;
import com.ismolka.validation.utils.change.value.ValueChangesCheckerResult;
import com.ismolka.validation.utils.change.value.ValueDifference;
import com.ismolka.validation.utils.constant.CollectionOperation;
import io.micrometer.common.util.StringUtils;
import org.antlr.v4.runtime.misc.OrderedHashSet;
import org.aspectj.weaver.ast.Or;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class DefaultCheckerResultNavigator implements CheckerResultNavigator {

    private final CheckerResult checkerResult;

    public DefaultCheckerResultNavigator(CheckerResult checkerResult) {
        this.checkerResult = checkerResult;
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


    private Difference getDifference(String fieldPath, Difference difference) {
        if (StringUtils.isBlank(fieldPath)) {
            return getDifference(difference);
        }

        String[] fields = fieldPath.split("\\.");

        Map<String, Difference> differenceMap = null;
        Difference lastDiff = difference;

        for (int i = 0; i < fields.length; i++) {
            String field = fields[i];

            if (differenceMap != null) {
                lastDiff = differenceMap.get(field);
            } else if (i == 0) {
                i--;
            }

            if (lastDiff == null) {
                return null;
            }

            Class<?> lastDiffClass = lastDiff.getClass();

            if (ValueDifference.class.isAssignableFrom(lastDiffClass)) {
                if (i != fields.length - 1) {
                    lastDiff = null;
                }
                break;
            }

            if (DifferenceRef.class.isAssignableFrom(lastDiffClass)) {
                DifferenceRef differenceRef = lastDiff.unwrap(DifferenceRef.class);

                if (i + 1 < fields.length && !Objects.equals(differenceRef.onField(), fields[i + 1])) {
                    return null;
                }

                lastDiff = differenceRef.toDifference();
                if (CollectionChangesCheckerResult.class.isAssignableFrom(lastDiff.getClass()) || MapChangesCheckerResult.class.isAssignableFrom(lastDiff.getClass())) {
                    throw new RuntimeException(String.format("Cannot extract result: unexpected collection or map on the path %s", field));
                }
                differenceMap = null;
                continue;
            }

            if (ValueChangesCheckerResult.class.isAssignableFrom(lastDiffClass)) {
                ValueChangesCheckerResult valueChangesCheckerResult = lastDiff.unwrap(ValueChangesCheckerResult.class);

                lastDiff = valueChangesCheckerResult;

                differenceMap = valueChangesCheckerResult.differenceMap();
                continue;
            }

            if (CollectionChangesCheckerResult.class.isAssignableFrom(lastDiffClass) || MapChangesCheckerResult.class.isAssignableFrom(lastDiffClass)) {
                if (i != fields.length - 1) {
                    throw new RuntimeException(String.format("Cannot extract result: unexpected collection or map on the path %s", field));
                } else {
                    return lastDiff;
                }
            }
        }

        return lastDiff;
    }

    private Difference getDifference(Difference difference) {
        Class<?> diffClass = difference.getClass();

        if (ValueChangesCheckerResult.class.isAssignableFrom(diffClass)) {
            return difference.unwrap(ValueChangesCheckerResult.class).differenceMap().get(null);
        }

//        if (ValueDifference.class.isAssignableFrom(diffClass) || CollectionChangesCheckerResult.class.isAssignableFrom(diffClass) || MapChangesCheckerResult.class.isAssignableFrom(diffClass)) {
//            return difference;
//        }

        if (DifferenceRef.class.isAssignableFrom(diffClass)) {
            throw new RuntimeException("Cannot extract result: unexpected reference");
        }

        return difference;
    }
}
