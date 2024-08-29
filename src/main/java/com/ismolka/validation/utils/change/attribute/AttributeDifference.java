package com.ismolka.validation.utils.change.attribute;

import com.ismolka.validation.utils.change.Difference;

import java.util.Objects;

public record AttributeDifference<F>(String field,
                                     Class<F> fieldRootClass,

                                     Class<?> fieldSourceClass,
                                     Class<?> fieldClass,
                                     F oldVal,
                                     F newVal) implements Difference {

    @Override
    public <T extends Difference> T unwrap(Class<T> type) {
        if (type.isAssignableFrom(AttributeDifference.class)) {
            return type.cast(this);
        }

        throw new ClassCastException(String.format("Cannot unwrap AttributeDifference to %s", type));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AttributeDifference<?> that = (AttributeDifference<?>) o;
        return Objects.equals(field, that.field) && Objects.equals(fieldRootClass, that.fieldRootClass) && Objects.equals(fieldClass, that.fieldClass) && Objects.equals(oldVal, that.oldVal) && Objects.equals(newVal, that.newVal);
    }

    @Override
    public int hashCode() {
        return Objects.hash(field, fieldRootClass, fieldClass, oldVal, newVal);
    }
}
