package com.ismolka.validation.constraints;

import com.ismolka.validation.validator.CheckUniqueAnnotationWithIgnoreMatchByIdValidatorField;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Describes a check for uniqueness with ignoring one match by PK field/fields.
 * Makes sense only if a {@link UniqueValidationConstraints} is present.
 * All information about check will be extracted from this annotation.
 * Main use-case: check for uniqueness in an update transaction.
 *
 * @author Ihar Smolka
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = CheckUniqueAnnotationWithIgnoreMatchByIdValidatorField.class)
public @interface CheckUniqueAnnotationWithIgnoreMatchById {

    String message() default "{com.ismolka.validation.constraints.CheckUniqueAnnotationWithIgnoreMatchById.message}";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };
}
