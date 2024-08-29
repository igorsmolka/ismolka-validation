package com.ismolka.validation.utils.change.attribute;

import com.ismolka.validation.utils.change.ChangesChecker;
import com.ismolka.validation.validator.metainfo.FieldPath;
import com.ismolka.validation.validator.utils.MetaInfoExtractorUtil;
import org.antlr.v4.runtime.misc.OrderedHashSet;

import java.lang.reflect.Method;
import java.util.Set;
import java.util.function.BiPredicate;

public class AttributeCheckDescriptorBuilder {

    Class<?> sourceClass;

    FieldPath fieldPath;

    Set<FieldPath> equalsFields;

    Method equalsMethodReflectionRef;

    BiPredicate biEqualsMethodCodeRef;

    ChangesChecker changesChecker;

    public static AttributeCheckDescriptorBuilder builder(Class<?> sourceClass) {
        return new AttributeCheckDescriptorBuilder(sourceClass);
    }

    public AttributeCheckDescriptorBuilder(Class<?> sourceClass) {
        this.sourceClass = sourceClass;
    }

    public AttributeCheckDescriptorBuilder field(String fieldPath) {
        this.fieldPath = MetaInfoExtractorUtil.extractFieldPathMetaInfo(fieldPath, sourceClass);

        return this;
    }

    public AttributeCheckDescriptorBuilder addFieldForEquals(String fieldForEqualsPath) {
        if (fieldPath == null) {
            throw new RuntimeException("Cannot add field for equals before initializing checking field");
        }

        if (equalsFields == null) {
            equalsFields = new OrderedHashSet<>();
        }

        Class<?> attributeClass = fieldPath.getLast().clazz();

        equalsFields.add(MetaInfoExtractorUtil.extractFieldPathMetaInfo(fieldForEqualsPath, attributeClass));

        return this;
    }

    public AttributeCheckDescriptorBuilder equalsReflectionMethod(Method equalsMethod) {
        this.equalsMethodReflectionRef = equalsMethod;

        return this;
    }

    public AttributeCheckDescriptorBuilder biEqualsMethod(BiPredicate biEqualsMethod) {
        this.biEqualsMethodCodeRef = biEqualsMethod;

        return this;
    }

    public AttributeCheckDescriptorBuilder changesChecker(ChangesChecker changesChecker) {
        this.changesChecker = changesChecker;

        return this;
    }

    public AttributeCheckDescriptor build() {
        return new AttributeCheckDescriptor(fieldPath, equalsFields, equalsMethodReflectionRef, biEqualsMethodCodeRef, changesChecker);
    }
}
