package com.ismolka.validation.utils.change;

import com.ismolka.validation.utils.change.attribute.AttributeChangesCheckerResult;

public interface ChangesChecker<T> {

    AttributeChangesCheckerResult getResult(T oldObj, T newObj);
}
