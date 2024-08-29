package com.ismolka.validation.utils.change.value;

import com.ismolka.validation.utils.change.ChangesChecker;
import com.ismolka.validation.validator.metainfo.FieldPath;
import com.ismolka.validation.validator.utils.MetaInfoExtractorUtil;
import org.antlr.v4.runtime.misc.OrderedHashSet;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Method;
import java.util.Set;
import java.util.function.BiPredicate;

public class ValueCheckDescriptorBuilder {

    Class<?> sourceClass;

    String attributePath;

    Set<String> equalsFields;

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
        this.attributePath = fieldPath;

        return this;
    }

    public ValueCheckDescriptorBuilder addFieldForEquals(String fieldForEqualsPath) {
        if (equalsFields == null) {
            equalsFields = new OrderedHashSet<>();
        }

        equalsFields.add(fieldForEqualsPath);

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
        validate();

        FieldPath attributeAsFieldPath = MetaInfoExtractorUtil.extractFieldPathMetaInfo(attributePath, sourceClass);

        Set<FieldPath> equalsFieldsAsFieldPaths = !CollectionUtils.isEmpty(equalsFields) ? MetaInfoExtractorUtil.extractFieldPathsMetaInfo(equalsFields.toArray(String[]::new), attributeAsFieldPath.getLast().clazz()) : new OrderedHashSet<>();

        return new ValueCheckDescriptor(attributeAsFieldPath, equalsFieldsAsFieldPaths, equalsMethodReflectionRef, biEqualsMethodCodeRef, changesChecker);
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
    }
}
