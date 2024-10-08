package com.ismolka.validation.constraints;

import com.ismolka.validation.constraints.inner.RelationCheckConstraint;
import com.ismolka.validation.validator.CheckRelationsExistsConstraintsValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Describes a check for relation existing.
 *
 * @author Ihar Smolka
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = CheckRelationsExistsConstraintsValidator.class)
public @interface CheckRelationsExistsConstraints {

    String message() default "{com.ismolka.validation.constraints.CheckRelationsExistsConstraints.message}";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };

    /**
     * @return constraints for relation existing check.
     */
    RelationCheckConstraint[] value();

    /**
     * @return flag to add a 'main' message to violations.
     */
    boolean addMessageToViolations() default false;
}
