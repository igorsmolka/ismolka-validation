package com.ismolka.validation.utils.change.value;

import com.ismolka.validation.utils.change.ChangesChecker;
import com.ismolka.validation.utils.metainfo.FieldPath;
import com.ismolka.validation.utils.metainfo.MetaInfoExtractorUtil;
import com.ismolka.validation.utils.reflection.ReflectUtil;
import org.antlr.v4.runtime.misc.OrderedHashSet;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Method;
import java.util.Set;
import java.util.function.BiPredicate;

public class ValueCheckDescriptorBuilder<Q> {

    Class<?> sourceClass;

    Class<Q> targetClass;

    String attributePath;

    Set<String> equalsFields;

    Method equalsMethodReflectionRef;

    BiPredicate<Q, Q> biEqualsMethodCodeRef;

    ChangesChecker<Q> changesChecker;

    public static <X> ValueCheckDescriptorBuilder<X> builder(Class<?> sourceClass, Class<X> targetClass) {
        return new ValueCheckDescriptorBuilder<>(sourceClass, targetClass);
    }

    public ValueCheckDescriptorBuilder(Class<?> sourceClass, Class<Q> targetClass) {
        this.sourceClass = sourceClass;
        this.targetClass = targetClass;
    }

    public ValueCheckDescriptorBuilder<Q> attribute(String fieldPath) {
        this.attributePath = fieldPath;

        return this;
    }

    public ValueCheckDescriptorBuilder<Q> addFieldForEquals(String fieldForEqualsPath) {
        if (equalsFields == null) {
            equalsFields = new OrderedHashSet<>();
        }

        equalsFields.add(fieldForEqualsPath);

        return this;
    }

    public ValueCheckDescriptorBuilder<Q> equalsReflectionMethod(Method equalsMethod) {
        this.equalsMethodReflectionRef = equalsMethod;

        return this;
    }

    public ValueCheckDescriptorBuilder<Q> biEqualsMethod(BiPredicate<Q, Q> biEqualsMethod) {
        this.biEqualsMethodCodeRef = biEqualsMethod;

        return this;
    }

    public ValueCheckDescriptorBuilder<Q> changesChecker(ChangesChecker<Q> changesChecker) {
        this.changesChecker = changesChecker;

        return this;
    }

    public ValueCheckDescriptor<Q> build() {
        validate();

        FieldPath attributeAsFieldPath = MetaInfoExtractorUtil.extractFieldPathMetaInfo(attributePath, sourceClass);

        Set<FieldPath> equalsFieldsAsFieldPaths = !CollectionUtils.isEmpty(equalsFields) ? MetaInfoExtractorUtil.extractFieldPathsMetaInfo(equalsFields.toArray(String[]::new), attributeAsFieldPath.getLast().clazz()) : new OrderedHashSet<>();

        return new ValueCheckDescriptor<>(attributeAsFieldPath, equalsFieldsAsFieldPaths, equalsMethodReflectionRef, biEqualsMethodCodeRef, changesChecker);
    }

    private void validate() {
        if (sourceClass == null) {
            throw new RuntimeException("Source class is not defined");
        }

        if (attributePath == null) {
            throw new RuntimeException("Cannot create descriptor without attribute");
        }

        if (!CollectionUtils.isEmpty(equalsFields) || changesChecker != null) {
            if (biEqualsMethodCodeRef != null || equalsMethodReflectionRef != null) {
                throw new RuntimeException("Cannot set global equals method when equals fields or changes checker are defined");
            }
        }

        if (biEqualsMethodCodeRef != null && equalsMethodReflectionRef != null) {
            throw new RuntimeException("Should be only one kind of defining global equals method for check description");
        }

        if (equalsMethodReflectionRef != null && ReflectUtil.methodIsNotPresent(equalsMethodReflectionRef, sourceClass)) {
            throw new RuntimeException(String.format("Source class %s doesnt declare the method %s", sourceClass, equalsMethodReflectionRef));
        }

        if (equalsFields != null) {
            equalsFields.forEach(equalsField -> {
                if (ReflectUtil.fieldPathIsNotPresent(equalsField, targetClass)) {
                    throw new RuntimeException(String.format("Equals field %s is not present in class %s", equalsField, targetClass));
                }
            });
        }
    }
}
