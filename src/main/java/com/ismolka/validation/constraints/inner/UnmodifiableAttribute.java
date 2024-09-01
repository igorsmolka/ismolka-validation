package com.ismolka.validation.constraints.inner;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Describes an unmodifiable attribute.
 *
 * @author Ihar Smolka
 */
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface UnmodifiableAttribute {

    /**
     * @return unmodifiable field.
     */
    String value();

    /**
     * @return custom equals method name (doesn't make sense, when equalsFields is specified).
     */
    String equalsMethodName() default "";

    /**
     * @return message for a violation.
     */
    String message() default "{com.ismolka.validation.constraints.inner.UnmodifiableAttribute.message}";

    /**
     * @return custom attribute naming for error messages.
     */
    String attributeErrorMessageNaming() default "";

    /**
     * @return fields for equals checking (doesn't make sense, when equalsMethodName is specified)
     */
    String[] equalsFields() default {};
}
