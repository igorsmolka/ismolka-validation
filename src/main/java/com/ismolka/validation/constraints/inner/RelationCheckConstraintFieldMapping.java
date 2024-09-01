package com.ismolka.validation.constraints.inner;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Describes a relation mapping.
 *
 * @author Ihar Smolka
 */
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface RelationCheckConstraintFieldMapping {

    /**
     * @return field from root entity.
     */
    String fromForeignKeyField();

    /**
     * @return field from foreign entity.
     */
    String toPrimaryKeyField();
}
