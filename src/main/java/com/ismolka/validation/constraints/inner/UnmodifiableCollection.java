package com.ismolka.validation.constraints.inner;

import com.ismolka.validation.constraints.constant.CollectionOperation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface UnmodifiableCollection {

    String value();

    String equalsMethodName() default "equals";

    Class<?> collectionGenericClass() default Object.class;

    String[] fieldsForMatching() default {};

    String message() default "{com.ismolka.validation.constraints.inner.UnmodifiableCollection.message}";

    String collectionErrorMessageNaming() default "";

    CollectionOperation[] forbiddenOperations() default { CollectionOperation.REMOVE, CollectionOperation.ADD, CollectionOperation.UPDATE };

    String[] equalsFields() default {};
}
