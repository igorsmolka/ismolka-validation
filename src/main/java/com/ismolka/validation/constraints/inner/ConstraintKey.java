package com.ismolka.validation.constraints.inner;

import java.lang.annotation.*;

@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ConstraintKey {
    String[] value();
}
