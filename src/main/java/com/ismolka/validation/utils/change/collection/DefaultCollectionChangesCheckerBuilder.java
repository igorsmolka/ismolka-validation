package com.ismolka.validation.utils.change.collection;

import com.ismolka.validation.constraints.constant.CollectionOperation;
import com.ismolka.validation.utils.change.attribute.AttributeMetaInfo;
import com.ismolka.validation.utils.change.attribute.DefaultAttributeChangesChecker;
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

    Set<AttributeMetaInfo> attributesToCheck;

    boolean stopOnFirstDiff;

    Set<CollectionOperation> forOperations;

    Method mainEqualsMethodReflectionRef;

    BiPredicate<T, T> mainBiEqualsMethodCodeRef;

    Set<FieldPath> fieldsForMatching;

    Set<FieldPath> mainEqualsFields;

    public static <T> DefaultCollectionChangesCheckerBuilder<T> builder(Class<T> collectionGenericClass) {
        return new DefaultCollectionChangesCheckerBuilder<>(collectionGenericClass);
    }


    private DefaultCollectionChangesCheckerBuilder(Class<T> collectionGenericClass) {
        this.collectionGenericClass = collectionGenericClass;
    }

    public DefaultCollectionChangesCheckerBuilder<T> addAttributeToCheck(AttributeMetaInfo attribute) {
        if (attributesToCheck == null) {
            attributesToCheck = new OrderedHashSet<>();
        }

        attributesToCheck.add(attribute);

        return this;
    }

    public DefaultCollectionChangesCheckerBuilder<T> stopOnFirstDiff() {
        this.stopOnFirstDiff = true;

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

    public DefaultCollectionChangesCheckerBuilder<T> mainMethodEqualsReflection(Method mainEqualsMethodReflectionRef) {
        this.mainEqualsMethodReflectionRef = mainEqualsMethodReflectionRef;

        return this;
    }


    public DefaultCollectionChangesCheckerBuilder<T> mainBiEqualsMethod(BiPredicate<T, T> mainBiEqualsMethodCodeRef) {
        this.mainBiEqualsMethodCodeRef = mainBiEqualsMethodCodeRef;

        return this;
    }

    public DefaultCollectionChangesCheckerBuilder<T> addFieldForMatching(String fieldPathForMatching) {
        if (fieldsForMatching == null) {
            fieldsForMatching = new OrderedHashSet<>();
        }

        FieldPath fieldForMatching = MetaInfoExtractorUtil.extractFieldPathMetaInfo(fieldPathForMatching, collectionGenericClass);

        fieldsForMatching.add(fieldForMatching);

        return this;
    }

    public DefaultCollectionChangesCheckerBuilder<T> addMainEqualsField(String mainEqualsFieldPath) {
        if (mainEqualsFields == null) {
            mainEqualsFields = new OrderedHashSet<>();
        }

        FieldPath fieldForMainEquals = MetaInfoExtractorUtil.extractFieldPathMetaInfo(mainEqualsFieldPath, collectionGenericClass);

        mainEqualsFields.add(fieldForMainEquals);

        return this;
    }

    public DefaultCollectionChangesChecker<T> build() {
        return new DefaultCollectionChangesChecker<>(collectionGenericClass, attributesToCheck, stopOnFirstDiff, forOperations, mainEqualsMethodReflectionRef, mainBiEqualsMethodCodeRef, fieldsForMatching, mainEqualsFields);
    }
}
