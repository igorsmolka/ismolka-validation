package com.ismolka.validation.validator;

import jakarta.validation.ConstraintValidatorContext;

public interface ValidationChainElement<T> {

    boolean isValid(T object, ConstraintValidatorContext context);
}
