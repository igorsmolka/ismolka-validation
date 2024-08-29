package com.ismolka.validation.utils.change.attribute;

import com.ismolka.validation.utils.change.Difference;

import java.util.Objects;

public record AttributeDifference<Q, F>(String valueFieldPath,
                                     Class<Q> valueFieldRootClass,

                                     Class<?> valueFieldSourceClass,
                                     Class<F> valueClass,
                                     F oldValue,
                                     F newValue) implements Difference {

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
        AttributeDifference<?, ?> that = (AttributeDifference<?, ?>) o;
        return Objects.equals(valueFieldPath, that.valueFieldPath) && Objects.equals(valueFieldRootClass, that.valueFieldRootClass) && Objects.equals(valueClass, that.valueClass) && Objects.equals(oldValue, that.oldValue) && Objects.equals(newValue, that.newValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(valueFieldPath, valueFieldRootClass, valueClass, oldValue, newValue);
    }
}
