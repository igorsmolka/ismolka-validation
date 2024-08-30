package com.ismolka.validation.constraints.inner;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface UnmodifiableAttribute {
    String value();

    String equalsMethodName() default "";

    String message() default "{com.ismolka.validation.constraints.inner.UnmodifiableAttribute.message}";

    String attributeErrorMessageNaming() default "";

    String[] equalsFields() default {};
}
