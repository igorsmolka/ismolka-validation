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

    String attribute;

    Set<String> equalsFields;

    Method equalsMethodReflection;

    BiPredicate<Q, Q> biEqualsMethod;

    ChangesChecker<Q> changesChecker;

    public static <X> ValueCheckDescriptorBuilder<X> builder(Class<?> sourceClass, Class<X> targetClass) {
        return new ValueCheckDescriptorBuilder<>(sourceClass, targetClass);
    }

    private ValueCheckDescriptorBuilder(Class<?> sourceClass, Class<Q> targetClass) {
        this.sourceClass = sourceClass;
        this.targetClass = targetClass;
    }

    public ValueCheckDescriptorBuilder<Q> attribute(String attribute) {
        this.attribute = attribute;

        return this;
    }

    public ValueCheckDescriptorBuilder<Q> addEqualsField(String fieldForEqualsPath) {
        if (equalsFields == null) {
            equalsFields = new OrderedHashSet<>();
        }

        equalsFields.add(fieldForEqualsPath);

        return this;
    }

    public ValueCheckDescriptorBuilder<Q> equalsMethodReflection(Method equalsMethod) {
        this.equalsMethodReflection = equalsMethod;

        return this;
    }

    public ValueCheckDescriptorBuilder<Q> biEqualsMethod(BiPredicate<Q, Q> biEqualsMethod) {
        this.biEqualsMethod = biEqualsMethod;

        return this;
    }

    public ValueCheckDescriptorBuilder<Q> changesChecker(ChangesChecker<Q> changesChecker) {
        this.changesChecker = changesChecker;

        return this;
    }

    public ValueCheckDescriptor<Q> build() {
        validate();

        FieldPath attributeAsFieldPath = MetaInfoExtractorUtil.extractFieldPathMetaInfo(attribute, sourceClass);

        Set<FieldPath> equalsFieldsAsFieldPaths = !CollectionUtils.isEmpty(equalsFields) ? MetaInfoExtractorUtil.extractFieldPathsMetaInfo(equalsFields.toArray(String[]::new), attributeAsFieldPath.getLast().clazz()) : new OrderedHashSet<>();

        return new ValueCheckDescriptor<>(attributeAsFieldPath, equalsFieldsAsFieldPaths, equalsMethodReflection, biEqualsMethod, changesChecker);
    }

    private void validate() {
        if (sourceClass == null) {
            throw new IllegalArgumentException("Source class is not defined");
        }

        if (attribute == null) {
            throw new IllegalArgumentException("Cannot create descriptor without attribute");
        }

        if (ReflectUtil.fieldPathIsNotPresent(attribute, sourceClass)) {
            throw new IllegalArgumentException(String.format("Field %s is not present in source class %s", attribute, sourceClass));
        }

        if (!CollectionUtils.isEmpty(equalsFields) || changesChecker != null) {
            if (biEqualsMethod != null || equalsMethodReflection != null) {
                throw new IllegalArgumentException("Cannot set global equals method when equals fields or changes checker are defined");
            }
        }

        if (biEqualsMethod != null && equalsMethodReflection != null) {
            throw new IllegalArgumentException("Should be only one kind of defining global equals method for check description");
        }

        if (equalsMethodReflection != null && ReflectUtil.methodIsNotPresent(equalsMethodReflection, sourceClass)) {
            throw new IllegalArgumentException(String.format("Source class %s doesnt declare the method %s", sourceClass, equalsMethodReflection));
        }

        if (equalsFields != null) {
            equalsFields.forEach(equalsField -> {
                if (ReflectUtil.fieldPathIsNotPresent(equalsField, targetClass)) {
                    throw new IllegalArgumentException(String.format("Equals field %s is not present in class %s", equalsField, targetClass));
                }
            });
        }
    }
}
