package com.ismolka.validation.utils.change.attribute;

import com.ismolka.validation.utils.change.ChangesChecker;
import com.ismolka.validation.validator.metainfo.FieldPath;
import com.ismolka.validation.validator.utils.MetaInfoExtractorUtil;
import org.antlr.v4.runtime.misc.OrderedHashSet;

import java.lang.reflect.Method;
import java.util.Set;
import java.util.function.BiPredicate;

public class ValueCheckDescriptorBuilder {

    Class<?> sourceClass;

    FieldPath attribute;

    Set<FieldPath> equalsFields;

    Method equalsMethodReflectionRef;

    BiPredicate biEqualsMethodCodeRef;

    ChangesChecker changesChecker;

    public static ValueCheckDescriptorBuilder builder(Class<?> sourceClass) {
        return new ValueCheckDescriptorBuilder(sourceClass);
    }

    public ValueCheckDescriptorBuilder(Class<?> sourceClass) {
        this.sourceClass = sourceClass;
    }

    public ValueCheckDescriptorBuilder attribute(String fieldPath) {
        this.attribute = MetaInfoExtractorUtil.extractFieldPathMetaInfo(fieldPath, sourceClass);

        return this;
    }

    public ValueCheckDescriptorBuilder addFieldForEquals(String fieldForEqualsPath) {
        if (attribute == null) {
            throw new RuntimeException("Cannot add field for equals before initializing checking field");
        }

        if (equalsFields == null) {
            equalsFields = new OrderedHashSet<>();
        }

        Class<?> attributeClass = attribute.getLast().clazz();

        equalsFields.add(MetaInfoExtractorUtil.extractFieldPathMetaInfo(fieldForEqualsPath, attributeClass));

        return this;
    }

    public ValueCheckDescriptorBuilder equalsReflectionMethod(Method equalsMethod) {
        this.equalsMethodReflectionRef = equalsMethod;

        return this;
    }

    public ValueCheckDescriptorBuilder biEqualsMethod(BiPredicate biEqualsMethod) {
        this.biEqualsMethodCodeRef = biEqualsMethod;

        return this;
    }

    public ValueCheckDescriptorBuilder changesChecker(ChangesChecker changesChecker) {
        this.changesChecker = changesChecker;

        return this;
    }

    public ValueCheckDescriptor build() {
        return new ValueCheckDescriptor(attribute, equalsFields, equalsMethodReflectionRef, biEqualsMethodCodeRef, changesChecker);
    }
}
