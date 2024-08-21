package com.ismolka.validation.constraints.inner;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface LimitValidationConstraintGroup {
    ConstraintKey[] constraintKeys();
    int limit() default 1;
}
