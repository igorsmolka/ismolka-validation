package com.ismolka.validation.constraints.inner;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Describes a limit constraint for the model.
 * Can contain multiple constraints.
 * Each constraint will be combined with others in a predicate using the OR operator.
 *
 * @author Ihar Smolka
 */
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface LimitValidationConstraintGroup {

    /**
     * @return array of field constraints.
     */
    ConstraintKey[] constraintKeys();

    /**
     * @return limit for all constraints.
     */
    int limit() default 1;
}
