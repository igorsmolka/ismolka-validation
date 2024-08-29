package com.ismolka.validation.utils.change.attribute;

import com.ismolka.validation.utils.change.ChangesChecker;
import com.ismolka.validation.validator.metainfo.FieldPath;
import com.ismolka.validation.validator.utils.MetaInfoExtractorUtil;
import org.antlr.v4.runtime.misc.OrderedHashSet;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiPredicate;

public record AttributeMetaInfo(
        FieldPath fieldPath,

        Set<FieldPath> equalsFields,

        Method equalsMethodReflectionRef,

        BiPredicate biEqualsMethodCodeRef,

        ChangesChecker changesChecker
) {

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AttributeMetaInfo that = (AttributeMetaInfo) o;
        return Objects.equals(fieldPath, that.fieldPath) && Objects.equals(equalsFields, that.equalsFields) && Objects.equals(equalsMethodReflectionRef, that.equalsMethodReflectionRef) && Objects.equals(biEqualsMethodCodeRef, that.biEqualsMethodCodeRef) && Objects.equals(changesChecker, that.changesChecker);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fieldPath, equalsFields, equalsMethodReflectionRef, biEqualsMethodCodeRef, changesChecker);
    }

    public static Builder builder(Class<?> sourceClass) {
        return new Builder(sourceClass);
    }

    public static class Builder {

        Class<?> sourceClass;

        FieldPath fieldPath;

        Set<FieldPath> equalsFields;

        Method equalsMethodReflectionRef;

        BiPredicate biEqualsMethodCodeRef;

        ChangesChecker changesChecker;

        public Builder(Class<?> sourceClass) {
            this.sourceClass = sourceClass;
        }

        public Builder field(String fieldPath) {
            this.fieldPath = MetaInfoExtractorUtil.extractFieldPathMetaInfo(fieldPath, sourceClass);

            return this;
        }

        public Builder addFieldForEquals(String fieldForEqualsPath) {
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

        public Builder equalsReflectionMethod(Method equalsMethod) {
            this.equalsMethodReflectionRef = equalsMethod;

            return this;
        }

        public Builder biEqualsMethod(BiPredicate biEqualsMethod) {
            this.biEqualsMethodCodeRef = biEqualsMethod;

            return this;
        }

        public Builder changesChecker(ChangesChecker changesChecker) {
            this.changesChecker = changesChecker;

            return this;
        }

        public AttributeMetaInfo build() {
            return new AttributeMetaInfo(fieldPath, equalsFields, equalsMethodReflectionRef, biEqualsMethodCodeRef, changesChecker);
        }
    }
}
