package com.ismolka.validation.constraints.inner;

import com.ismolka.validation.utils.constant.CollectionOperation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Describes a unmodifiable collection.
 *
 * @author Ihar Smolka
 */
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface UnmodifiableCollection {

    /**
     * @return unmodifiable collection field.
     */
    String value();

    /**
     * @return custom equals method name (doesn't make sense, when equalsFields is specified).
     */
    String equalsMethodName() default "";

    /**
     * @return class of collection inner elements.
     */
    Class<?> collectionGenericClass() default Object.class;

    /**
     * @return fields for matching elements within a collection (if specified and fields are equal between elements of two collections - then validator will check the elements for equality. If is not specified - then indexes in the collections will be used as a 'key').
     */
    String[] fieldsForMatching() default {};

    /**
     * @return message for a violation.
     */
    String message() default "{com.ismolka.validation.constraints.inner.UnmodifiableCollection.message}";

    /**
     * @return custom collection naming for error messages.
     */
    String collectionErrorMessageNaming() default "";

    /**
     * @return forbidden modify operations for collection.
     */
    CollectionOperation[] forbiddenOperations() default { CollectionOperation.REMOVE, CollectionOperation.ADD, CollectionOperation.UPDATE };

    /**
     * @return fields for equals checking (doesn't make sense, when equalsMethodName is specified)
     */
    String[] equalsFields() default {};
}
