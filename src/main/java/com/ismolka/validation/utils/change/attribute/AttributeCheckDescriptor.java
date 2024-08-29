package com.ismolka.validation.utils.change.attribute;

import com.ismolka.validation.utils.change.ChangesChecker;
import com.ismolka.validation.validator.metainfo.FieldPath;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiPredicate;

public record AttributeCheckDescriptor(
        FieldPath attribute,

        Set<FieldPath> equalsFields,

        Method equalsMethodReflectionRef,

        BiPredicate biEqualsMethodCodeRef,

        ChangesChecker changesChecker
) {

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AttributeCheckDescriptor that = (AttributeCheckDescriptor) o;
        return Objects.equals(attribute, that.attribute) && Objects.equals(equalsFields, that.equalsFields) && Objects.equals(equalsMethodReflectionRef, that.equalsMethodReflectionRef) && Objects.equals(biEqualsMethodCodeRef, that.biEqualsMethodCodeRef) && Objects.equals(changesChecker, that.changesChecker);
    }

    @Override
    public int hashCode() {
        return Objects.hash(attribute, equalsFields, equalsMethodReflectionRef, biEqualsMethodCodeRef, changesChecker);
    }
}
