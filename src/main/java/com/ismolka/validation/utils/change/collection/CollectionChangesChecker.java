package com.ismolka.validation.utils.change.collection;

import com.ismolka.validation.utils.change.ChangesChecker;

import java.util.Collection;

public interface CollectionChangesChecker<T> extends ChangesChecker<T> {

    CollectionChangesCheckerResult<T> getResult(Collection<T> oldCollection, Collection<T> newCollection);
}
