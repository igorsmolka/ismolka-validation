package com.ismolka.validation.utils.change.navigator;

import com.ismolka.validation.utils.change.Difference;
import com.ismolka.validation.utils.change.collection.CollectionElementDifference;
import com.ismolka.validation.utils.constant.CollectionOperation;

import java.util.Set;

public interface CheckerResultNavigator {


    <T> Set<CollectionElementDifference<T>> getDifferenceForCollection(String fieldPath, Class<T> forClass, CollectionOperation... operations);
    Difference getDifference(String fieldPath);
}
