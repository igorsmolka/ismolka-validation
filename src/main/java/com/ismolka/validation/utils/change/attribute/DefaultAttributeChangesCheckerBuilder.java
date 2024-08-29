package com.ismolka.validation.utils.change.attribute;

import com.ismolka.validation.utils.change.collection.DefaultCollectionChangesCheckerBuilder;
import com.ismolka.validation.validator.metainfo.FieldPath;
import com.ismolka.validation.validator.utils.MetaInfoExtractorUtil;
import org.antlr.v4.runtime.misc.OrderedHashSet;

import java.lang.reflect.Method;
import java.util.Set;
import java.util.function.BiPredicate;

public class DefaultAttributeChangesCheckerBuilder<T> {

    Class<T> clazz;

    Set<AttributeCheckDescriptor> attributesCheckDescriptors;

    boolean stopOnFirstDiff;

    Method globalEqualsMethodReflectionRef;

    BiPredicate<T, T> globalBiEqualsMethodCodeRef;

    Set<FieldPath> globalEqualsFields;

    public static <T> DefaultAttributeChangesCheckerBuilder<T> builder(Class<T> clazz) {
        return new DefaultAttributeChangesCheckerBuilder<>(clazz);
    }

    private DefaultAttributeChangesCheckerBuilder(Class<T> clazz) {
        this.clazz = clazz;
    }

    public DefaultAttributeChangesCheckerBuilder<T> addAttributeToCheck(AttributeCheckDescriptor attribute) {
        if (attributesCheckDescriptors == null) {
            attributesCheckDescriptors = new OrderedHashSet<>();
        }

        attributesCheckDescriptors.add(attribute);

        return this;
    }

    public DefaultAttributeChangesCheckerBuilder<T> stopOnFirstDiff() {
        this.stopOnFirstDiff = true;

        return this;
    }

    public DefaultAttributeChangesCheckerBuilder<T> globalEqualsMethodReflectionRef(Method globalEqualsMethodReflectionRef) {
        this.globalEqualsMethodReflectionRef = globalEqualsMethodReflectionRef;

        return this;
    }

    public DefaultAttributeChangesCheckerBuilder<T> globalBiEqualsMethodCodeRef(BiPredicate<T, T> globalBiEqualsMethodCodeRef) {
        this.globalBiEqualsMethodCodeRef = globalBiEqualsMethodCodeRef;

        return this;
    }

    public DefaultAttributeChangesCheckerBuilder<T> addGlobalEqualsField(String globalEqualsField) {
        if (globalEqualsFields == null) {
            globalEqualsFields = new OrderedHashSet<>();
        }

        FieldPath fieldForMainEquals = MetaInfoExtractorUtil.extractFieldPathMetaInfo(globalEqualsField, clazz);

        globalEqualsFields.add(fieldForMainEquals);

        return this;
    }

    public DefaultAttributeChangesChecker<T> build() {
        return new DefaultAttributeChangesChecker<>(attributesCheckDescriptors, stopOnFirstDiff, globalEqualsMethodReflectionRef, globalBiEqualsMethodCodeRef, globalEqualsFields);
    }
}
