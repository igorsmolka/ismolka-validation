package com.ismolka.validation.constraints;

import com.ismolka.validation.constraints.inner.LimitValidationConstraintGroup;
import com.ismolka.validation.validator.LimitValidationConstraintValidatorField;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = LimitValidationConstraintValidatorField.class)
public @interface LimitValidationConstraints {

    String message() default "{com.ismolka.validation.constraints.LimitConstraint.message}";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };

    LimitValidationConstraintGroup[] limitValueConstraints() default {};
}
