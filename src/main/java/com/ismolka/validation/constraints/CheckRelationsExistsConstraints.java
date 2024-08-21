package com.ismolka.validation.constraints;

import com.ismolka.validation.constraints.inner.RelationCheckConstraint;
import com.ismolka.validation.validator.CheckRelationsExistsConstraintsValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = CheckRelationsExistsConstraintsValidator.class)
public @interface CheckRelationsExistsConstraints {

    String message() default "{com.ismolka.validation.constraints.CheckRelationsExistsConstraints.message}";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };

    RelationCheckConstraint[] value();
}
