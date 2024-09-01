package com.ismolka.validation.constraints;

import com.ismolka.validation.constraints.inner.ConstraintKey;
import com.ismolka.validation.validator.UniqueValidationConstraintValidatorField;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Describes a check for uniqueness.
 * Each constraint will be combined with others in a predicate using the OR operator.
 *
 * @author Ihar Smolka
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = UniqueValidationConstraintValidatorField.class)
public @interface UniqueValidationConstraints {

    String message() default "{com.ismolka.validation.constraints.UniqueValidationConstraint.message}";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };

    /**
     * @return unique constraints
     */
    ConstraintKey[] constraintKeys() default {};
}
