package com.ismolka.validation.utils.change;

import com.ismolka.validation.utils.change.value.ValueChangesCheckerResult;

public interface ChangesChecker<T> {

    ValueChangesCheckerResult getResult(T oldObj, T newObj);
}
