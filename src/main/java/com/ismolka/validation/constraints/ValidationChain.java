package com.ismolka.validation.constraints;

import com.ismolka.validation.validator.ValidationChainElement;
import com.ismolka.validation.validator.ValidationChainValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidationChainValidator.class)
public @interface ValidationChain {

    String message() default "{com.ismolka.validation.constraints.ValidationChain.message}";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };

    Class<? extends ValidationChainElement<?>>[] value() default {};

    boolean ignoreMainMessage() default false;
}
