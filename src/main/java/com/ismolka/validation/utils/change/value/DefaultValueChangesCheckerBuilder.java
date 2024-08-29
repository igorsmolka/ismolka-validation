package com.ismolka.validation.utils.change.value;

import com.ismolka.validation.validator.metainfo.FieldPath;
import com.ismolka.validation.validator.utils.MetaInfoExtractorUtil;
import org.antlr.v4.runtime.misc.OrderedHashSet;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Method;
import java.util.Set;
import java.util.function.BiPredicate;

public class DefaultValueChangesCheckerBuilder<T> {

    Class<T> clazz;

    Set<ValueCheckDescriptor> attributesCheckDescriptors;

    boolean stopOnFirstDiff;

    Method globalEqualsMethodReflectionRef;

    BiPredicate<T, T> globalBiEqualsMethodCodeRef;

    Set<FieldPath> globalEqualsFields;

    public static <T> DefaultValueChangesCheckerBuilder<T> builder(Class<T> clazz) {
        return new DefaultValueChangesCheckerBuilder<>(clazz);
    }

    private DefaultValueChangesCheckerBuilder(Class<T> clazz) {
        this.clazz = clazz;
    }

    public DefaultValueChangesCheckerBuilder<T> addAttributeToCheck(ValueCheckDescriptor attribute) {
        if (attributesCheckDescriptors == null) {
            attributesCheckDescriptors = new OrderedHashSet<>();
        }

        attributesCheckDescriptors.add(attribute);

        return this;
    }

    public DefaultValueChangesCheckerBuilder<T> stopOnFirstDiff() {
        this.stopOnFirstDiff = true;

        return this;
    }

    public DefaultValueChangesCheckerBuilder<T> globalEqualsMethodReflectionRef(Method globalEqualsMethodReflectionRef) {
        this.globalEqualsMethodReflectionRef = globalEqualsMethodReflectionRef;

        return this;
    }

    public DefaultValueChangesCheckerBuilder<T> globalBiEqualsMethodCodeRef(BiPredicate<T, T> globalBiEqualsMethodCodeRef) {
        this.globalBiEqualsMethodCodeRef = globalBiEqualsMethodCodeRef;

        return this;
    }

    public DefaultValueChangesCheckerBuilder<T> addGlobalEqualsField(String globalEqualsField) {
        if (globalEqualsFields == null) {
            globalEqualsFields = new OrderedHashSet<>();
        }

        FieldPath fieldForMainEquals = MetaInfoExtractorUtil.extractFieldPathMetaInfo(globalEqualsField, clazz);

        globalEqualsFields.add(fieldForMainEquals);

        return this;
    }

    public DefaultValueChangesChecker<T> build() {
        validate();

        return new DefaultValueChangesChecker<>(attributesCheckDescriptors, stopOnFirstDiff, globalEqualsMethodReflectionRef, globalBiEqualsMethodCodeRef, globalEqualsFields);
    }

    private void validate() {
        if (!CollectionUtils.isEmpty(attributesCheckDescriptors) || !CollectionUtils.isEmpty(globalEqualsFields)) {
            if (globalBiEqualsMethodCodeRef != null || globalEqualsMethodReflectionRef != null) {
                throw new RuntimeException("Cannot set global equals method when attribute check descriptors or equals fields are defined");
            }
        }
    }
}
