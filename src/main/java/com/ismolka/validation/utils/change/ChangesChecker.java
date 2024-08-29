package com.ismolka.validation.utils.change;

import com.ismolka.validation.utils.change.attribute.ValueChangesCheckerResult;

public interface ChangesChecker<T> {

    ValueChangesCheckerResult getResult(T oldObj, T newObj);
}
