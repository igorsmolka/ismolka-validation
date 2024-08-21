package com.ismolka.validation.constraints;

import com.ismolka.validation.constraints.inner.ConstraintKey;
import com.ismolka.validation.validator.UniqueValidationConstraintValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = UniqueValidationConstraintValidator.class)
public @interface UniqueValidationConstraints {

    String message() default "{com.ismolka.validation.constraints.UniqueValidationConstraint.message}";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };

    ConstraintKey[] constraintKeys() default {};
}
