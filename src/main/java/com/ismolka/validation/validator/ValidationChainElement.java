package com.ismolka.validation.validator;

import jakarta.validation.ConstraintValidatorContext;

/**
 * Interface for a validation chain element.
 *
 * @param <T> - type for validation.
 * @author Ihar Smolka
 */
public interface ValidationChainElement<T> {

    /**
     * Validation method
     *
     * @param object - value for validation
     * @param context - {@link ConstraintValidatorContext}
     * @return validation result
     */
    boolean isValid(T object, ConstraintValidatorContext context);
}
