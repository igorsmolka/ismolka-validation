package com.ismolka.validation.utils.change.collection;

import com.ismolka.validation.utils.constant.CollectionOperation;
import com.ismolka.validation.utils.change.value.ValueCheckDescriptor;
import com.ismolka.validation.utils.metainfo.FieldPath;
import com.ismolka.validation.utils.metainfo.MetaInfoExtractorUtil;
import com.ismolka.validation.utils.reflection.ReflectUtil;
import org.antlr.v4.runtime.misc.OrderedHashSet;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiPredicate;

/**
 * Builder for {@link DefaultCollectionChangesChecker}
 *
 * @param <T> - type of collection elements.
 *
 * @author Ihar Smolka
 */
public class DefaultCollectionChangesCheckerBuilder<T> {

    Class<T> collectionGenericClass;

    Set<ValueCheckDescriptor<?>> attributesCheckDescriptors;

    boolean stopOnFirstDiff;

    Set<CollectionOperation> forOperations;

    Set<String> fieldsForMatching;

    Method globalEqualsMethodReflection;

    BiPredicate<T, T> globalBiEqualsMethod;

    Set<String> globalEqualsFields;


    /**
     * Static method for builder instantiation.
     *
     * @param collectionGenericClass - class of collection elements.
     * @return builder
     * @param <T> - type of collection elements.
     */
    public static <T> DefaultCollectionChangesCheckerBuilder<T> builder(Class<T> collectionGenericClass) {
        return new DefaultCollectionChangesCheckerBuilder<>(collectionGenericClass);
    }


    private DefaultCollectionChangesCheckerBuilder(Class<T> collectionGenericClass) {
        this.collectionGenericClass = collectionGenericClass;
    }

    /**
     * {@link ValueCheckDescriptor} for attribute check.
     *
     * @param attribute - descriptor
     * @return this
     */
    public DefaultCollectionChangesCheckerBuilder<T> addAttributeToCheck(ValueCheckDescriptor<?> attribute) {
        if (attributesCheckDescriptors == null) {
            attributesCheckDescriptors = new OrderedHashSet<>();
        }

        attributesCheckDescriptors.add(attribute);

        return this;
    }

    /**
     * Flag to stop finding on first diff
     * @return this
     */
    public DefaultCollectionChangesCheckerBuilder<T> stopOnFirstDiff() {
        this.stopOnFirstDiff = true;

        return this;
    }

    /**
     * Equals method
     *
     * @param globalEqualsMethodReflectionRef - {@link Method}
     * @return this
     */
    public DefaultCollectionChangesCheckerBuilder<T> globalEqualsMethodReflection(Method globalEqualsMethodReflectionRef) {
        this.globalEqualsMethodReflection = globalEqualsMethodReflectionRef;

        return this;
    }

    /**
     * Equals method with two arguments
     *
     * @param globalBiEqualsMethodCodeRef - {@link BiPredicate}
     * @return this
     */
    public DefaultCollectionChangesCheckerBuilder<T> globalBiEqualsMethodCode(BiPredicate<T, T> globalBiEqualsMethodCodeRef) {
        this.globalBiEqualsMethod = globalBiEqualsMethodCodeRef;

        return this;
    }

    /**
     * Equals field
     *
     * @param globalEqualsField - field for equals
     * @return this
     */
    public DefaultCollectionChangesCheckerBuilder<T> addGlobalEqualsField(String globalEqualsField) {
        if (globalEqualsFields == null) {
            globalEqualsFields = new OrderedHashSet<>();
        }

        globalEqualsFields.add(globalEqualsField);

        return this;
    }

    /**
     * Fields for matching elements within a collection (if specified and fields are equal between elements of two collections - then validator will check the elements for equality. If is not specified - then indexes in the collections will be used as a 'key')
     *
     * @param fieldForMatching - field for matching
     * @return this
     */
    public DefaultCollectionChangesCheckerBuilder<T> addFieldForMatching(String fieldForMatching) {
        if (fieldsForMatching == null) {
            fieldsForMatching = new OrderedHashSet<>();
        }

        fieldsForMatching.add(fieldForMatching);

        return this;
    }

    /**
     * Check for {@link CollectionOperation}
     *
     * @param operation - {@link CollectionOperation}
     * @return this
     */
    public DefaultCollectionChangesCheckerBuilder<T> forOperation(CollectionOperation operation) {
        if (forOperations == null) {
            forOperations = new HashSet<>();
        }

        forOperations.add(operation);

        return this;
    }

    /**
     * Check for {@link CollectionOperation}
     *
     * @param operations - {@link CollectionOperation}
     * @return this
     */
    public DefaultCollectionChangesCheckerBuilder<T> forOperations(CollectionOperation... operations) {
        if (forOperations == null) {
            forOperations = new HashSet<>();
        }

        forOperations.addAll(Arrays.stream(operations).toList());

        return this;
    }

    public DefaultCollectionChangesChecker<T> build() {
        if (CollectionUtils.isEmpty(forOperations)) {
            forOperations = new HashSet<>(Arrays.stream(CollectionOperation.values()).toList());
        }

        validate();

        Set<FieldPath> globalEqualsFieldsAsFieldPaths = !CollectionUtils.isEmpty(globalEqualsFields) ? MetaInfoExtractorUtil.extractFieldPathsMetaInfo(globalEqualsFields.toArray(String[]::new), collectionGenericClass) : new OrderedHashSet<>();

        Set<FieldPath> fieldsForMatchingAsFieldPaths = !CollectionUtils.isEmpty(fieldsForMatching) ? MetaInfoExtractorUtil.extractFieldPathsMetaInfo(fieldsForMatching.toArray(String[]::new), collectionGenericClass) : new OrderedHashSet<>();

        return new DefaultCollectionChangesChecker<>(collectionGenericClass, attributesCheckDescriptors, stopOnFirstDiff, globalEqualsMethodReflection, globalBiEqualsMethod, globalEqualsFieldsAsFieldPaths, forOperations, fieldsForMatchingAsFieldPaths);
    }

    private void validate() {
        if (collectionGenericClass == null) {
            throw new IllegalArgumentException("Collection generic class is not defined");
        }

        if (!CollectionUtils.isEmpty(attributesCheckDescriptors) || !CollectionUtils.isEmpty(globalEqualsFields)) {
            if (globalBiEqualsMethod != null || globalEqualsMethodReflection != null) {
                throw new IllegalArgumentException("Cannot set global equals method when attribute check descriptors or equals fields are defined");
            }
        }

        if (globalBiEqualsMethod != null && globalEqualsMethodReflection != null) {
            throw new IllegalArgumentException("Should be only one kind of defining global equals method for collection check");
        }

        if (globalEqualsMethodReflection != null) {
            if (ReflectUtil.methodIsNotPresent(globalEqualsMethodReflection, collectionGenericClass)) {
                throw new IllegalArgumentException(String.format("Collection class %s doesnt declare the method %s", collectionGenericClass, globalEqualsMethodReflection));
            }

            if (!globalEqualsMethodReflection.getReturnType().equals(boolean.class) && !globalEqualsMethodReflection.getReturnType().equals(Boolean.class)) {
                throw new IllegalArgumentException("Equals method must return boolean");
            }
        }

        if (globalEqualsFields != null) {
            globalEqualsFields.forEach(equalsField -> {
                if (ReflectUtil.fieldPathIsNotPresent(equalsField, collectionGenericClass)) {
                    throw new IllegalArgumentException(String.format("Equals field %s is not present in collection generic class %s", equalsField, collectionGenericClass));
                }
            });
        }
    }
}
