package com.ismolka.validation.constraints.inner;

import java.lang.annotation.*;

/**
 * Describes a constraint key for the model.
 * Can contain multiple attributes.
 * Every attribute can be specified as a path, for example 'book.inventoryNumber'.
 *
 * @author Ihar Smolka
 */
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ConstraintKey {

    /**
     * @return array of attributes describing the constraint.
     */
    String[] value();
}
