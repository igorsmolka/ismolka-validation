package com.ismolka.validation.constraints;

import com.ismolka.validation.validator.ValidationChainElement;
import com.ismolka.validation.validator.ValidationChainValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Describes a validation chain for a model.
 * @see ValidationChainElement
 *
 * @author Ihar Smolka
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidationChainValidator.class)
public @interface ValidationChain {

    String message() default "{com.ismolka.validation.constraints.ValidationChain.message}";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };

    /**
     * @return bean classes for validation chain (should implement a {@link ValidationChainElement} interface).
     */
    Class<? extends ValidationChainElement<?>>[] value() default {};

    /**
     * @return flag to add a 'main' message to violations.
     */
    boolean addMessageToViolations() default false;
}
