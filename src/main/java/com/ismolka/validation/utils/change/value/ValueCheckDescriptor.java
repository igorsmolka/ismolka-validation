package com.ismolka.validation.utils.change.value;

import com.ismolka.validation.utils.change.ChangesChecker;
import com.ismolka.validation.utils.metainfo.FieldPath;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiPredicate;

public record ValueCheckDescriptor<Q>(
        FieldPath attribute,

        Set<FieldPath> equalsFields,

        Method equalsMethodReflection,

        BiPredicate<Q, Q> biEqualsMethod,

        ChangesChecker<Q> changesChecker
) {

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ValueCheckDescriptor<?> that = (ValueCheckDescriptor<?>) o;
        return Objects.equals(attribute, that.attribute) && Objects.equals(equalsFields, that.equalsFields) && Objects.equals(equalsMethodReflection, that.equalsMethodReflection) && Objects.equals(biEqualsMethod, that.biEqualsMethod) && Objects.equals(changesChecker, that.changesChecker);
    }

    @Override
    public int hashCode() {
        return Objects.hash(attribute, equalsFields, equalsMethodReflection, biEqualsMethod, changesChecker);
    }
}
