package com.ismolka.validation.constraints.inner;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Describes a constraint for relation existing check.
 * relationField or relationMapping should be specified for check.
 * If relationField is not specified - then validator will use a relationClass for extracting information about relation model.
 * If relationMapping is specified - then validator will use it for check. If not - then validator will use PK field/fields from relationField for existing check.
 * Every field can be specified as an attribute path.
 *
 * @author Ihar Smolka
 */
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface RelationCheckConstraint {

    /**
     * @return field in the model, which represents information about relation (by {@link jakarta.persistence.JoinColumn}, {@link jakarta.persistence.JoinColumns}).
     */
    String relationField() default "";

    /**
     * @return information about relation mapping.
     */
    RelationCheckConstraintFieldMapping[] relationMapping() default {};

    /**
     * @return relation model class.
     */
    Class<?> relationClass() default Object.class;

    /**
     * @return message for a violation
     */
    String message() default "{com.ismolka.validation.constraints.inner.RelationCheckConstraint.message}";

    /**
     * @return custom relation naming for error messages.
     */
    String relationErrorMessageNaming() default "";
}
