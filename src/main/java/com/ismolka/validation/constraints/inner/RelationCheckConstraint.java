package com.ismolka.validation.constraints.inner;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface RelationCheckConstraint {

    String relationField() default "";

    RelationCheckConstraintFieldMapping[] relationMapping() default {};

    Class<?> relationClass() default Object.class;

    String message() default "{com.ismolka.validation.constraints.inner.RelationCheckConstraint.message}";

    String relationErrorMessageNaming() default "";
}
