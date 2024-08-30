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

public class DefaultCollectionChangesCheckerBuilder<T> {

    Class<T> collectionGenericClass;

    Set<ValueCheckDescriptor<?>> attributesCheckDescriptors;

    boolean stopOnFirstDiff;

    Set<CollectionOperation> forOperations;

    Set<String> fieldsForMatching;

    Method globalEqualsMethodReflectionRef;

    BiPredicate<T, T> globalBiEqualsMethodCodeRef;

    Set<String> globalEqualsFields;

    public static <T> DefaultCollectionChangesCheckerBuilder<T> builder(Class<T> collectionGenericClass) {
        return new DefaultCollectionChangesCheckerBuilder<>(collectionGenericClass);
    }


    private DefaultCollectionChangesCheckerBuilder(Class<T> collectionGenericClass) {
        this.collectionGenericClass = collectionGenericClass;
    }

    public DefaultCollectionChangesCheckerBuilder<T> addAttributeToCheck(ValueCheckDescriptor<?> attribute) {
        if (attributesCheckDescriptors == null) {
            attributesCheckDescriptors = new OrderedHashSet<>();
        }

        attributesCheckDescriptors.add(attribute);

        return this;
    }

    public DefaultCollectionChangesCheckerBuilder<T> stopOnFirstDiff() {
        this.stopOnFirstDiff = true;

        return this;
    }

    public DefaultCollectionChangesCheckerBuilder<T> globalEqualsMethodReflectionRef(Method globalEqualsMethodReflectionRef) {
        this.globalEqualsMethodReflectionRef = globalEqualsMethodReflectionRef;

        return this;
    }

    public DefaultCollectionChangesCheckerBuilder<T> globalBiEqualsMethodCodeRef(BiPredicate<T, T> globalBiEqualsMethodCodeRef) {
        this.globalBiEqualsMethodCodeRef = globalBiEqualsMethodCodeRef;

        return this;
    }

    public DefaultCollectionChangesCheckerBuilder<T> addGlobalEqualsField(String globalEqualsField) {
        if (globalEqualsFields == null) {
            globalEqualsFields = new OrderedHashSet<>();
        }

        globalEqualsFields.add(globalEqualsField);

        return this;
    }

    public DefaultCollectionChangesCheckerBuilder<T> addFieldForMatching(String fieldForMatching) {
        if (fieldsForMatching == null) {
            fieldsForMatching = new OrderedHashSet<>();
        }

        fieldsForMatching.add(fieldForMatching);

        return this;
    }

    public DefaultCollectionChangesCheckerBuilder<T> forOperation(CollectionOperation operation) {
        if (forOperations == null) {
            forOperations = new HashSet<>();
        }

        forOperations.add(operation);

        return this;
    }

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

        return new DefaultCollectionChangesChecker<>(collectionGenericClass, attributesCheckDescriptors, stopOnFirstDiff, globalEqualsMethodReflectionRef, globalBiEqualsMethodCodeRef, globalEqualsFieldsAsFieldPaths, forOperations, fieldsForMatchingAsFieldPaths);
    }

    private void validate() {
        if (collectionGenericClass == null) {
            throw new RuntimeException("Collection generic class is not defined");
        }

        if (!CollectionUtils.isEmpty(attributesCheckDescriptors) || !CollectionUtils.isEmpty(globalEqualsFields)) {
            if (globalBiEqualsMethodCodeRef != null || globalEqualsMethodReflectionRef != null) {
                throw new RuntimeException("Cannot set global equals method when attribute check descriptors or equals fields are defined");
            }
        }

        if (globalBiEqualsMethodCodeRef != null && globalEqualsMethodReflectionRef != null) {
            throw new RuntimeException("Should be only one kind of defining global equals method for collection check");
        }

        if (globalEqualsMethodReflectionRef != null && ReflectUtil.methodIsNotPresent(globalEqualsMethodReflectionRef, collectionGenericClass)) {
            throw new RuntimeException(String.format("Collection class %s doesnt declare the method %s", collectionGenericClass, globalEqualsMethodReflectionRef));
        }

        if (globalEqualsFields != null) {
            globalEqualsFields.forEach(equalsField -> {
                if (ReflectUtil.fieldPathIsNotPresent(equalsField, collectionGenericClass)) {
                    throw new RuntimeException(String.format("Equals field %s is not present in collection generic class %s", equalsField, collectionGenericClass));
                }
            });
        }
    }
}
