package com.ismolka.validation.utils.change.collection;

import com.ismolka.validation.utils.change.attribute.DefaultAttributeChangesChecker;
import com.ismolka.validation.utils.change.attribute.DefaultAttributeChangesCheckerBuilder;
import com.ismolka.validation.utils.change.constant.CollectionOperation;
import com.ismolka.validation.utils.change.attribute.AttributeCheckDescriptor;
import com.ismolka.validation.validator.metainfo.FieldPath;
import com.ismolka.validation.validator.utils.MetaInfoExtractorUtil;
import org.antlr.v4.runtime.misc.OrderedHashSet;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiPredicate;

public class DefaultCollectionChangesCheckerBuilder<T> {

    Class<T> collectionGenericClass;

    Set<AttributeCheckDescriptor> attributesCheckDescriptors;

    boolean stopOnFirstDiff;

    Set<CollectionOperation> forOperations;

    Set<FieldPath> fieldsForMatching;

    Method globalEqualsMethodReflectionRef;

    BiPredicate<T, T> globalBiEqualsMethodCodeRef;

    Set<FieldPath> globalEqualsFields;

    public static <T> DefaultCollectionChangesCheckerBuilder<T> builder(Class<T> collectionGenericClass) {
        return new DefaultCollectionChangesCheckerBuilder<>(collectionGenericClass);
    }


    private DefaultCollectionChangesCheckerBuilder(Class<T> collectionGenericClass) {
        this.collectionGenericClass = collectionGenericClass;
    }

    public DefaultCollectionChangesCheckerBuilder<T> addAttributeToCheck(AttributeCheckDescriptor attribute) {
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

        FieldPath fieldForMainEquals = MetaInfoExtractorUtil.extractFieldPathMetaInfo(globalEqualsField, collectionGenericClass);

        globalEqualsFields.add(fieldForMainEquals);

        return this;
    }

    public DefaultCollectionChangesCheckerBuilder<T> addFieldForMatching(String fieldForMatching) {
        if (fieldsForMatching == null) {
            fieldsForMatching = new OrderedHashSet<>();
        }

        FieldPath fieldForMatchingPath = MetaInfoExtractorUtil.extractFieldPathMetaInfo(fieldForMatching, collectionGenericClass);

        fieldsForMatching.add(fieldForMatchingPath);

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

    public DefaultCollectionChangesCheckerBuilder<T> forAllOperations() {
        if (forOperations == null) {
            forOperations = new HashSet<>();
        }

        forOperations.addAll(Arrays.stream(CollectionOperation.values()).toList());

        return this;
    }

    public DefaultCollectionChangesChecker<T> build() {
        return new DefaultCollectionChangesChecker<>(attributesCheckDescriptors, stopOnFirstDiff, globalEqualsMethodReflectionRef, globalBiEqualsMethodCodeRef, globalEqualsFields, forOperations, fieldsForMatching);
    }
}
