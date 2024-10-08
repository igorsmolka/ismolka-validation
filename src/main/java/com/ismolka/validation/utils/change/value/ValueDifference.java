package com.ismolka.validation.utils.change.value;

import com.ismolka.validation.utils.change.Difference;

import java.util.Objects;

/**
 * Difference between two values.
 *
 * @param valueFieldPath - attribute path from the root class.
 * @param valueFieldRootClass - attribute root class.
 * @param valueFieldDeclaringClass - attribute declaring class.
 * @param valueClass - value class.
 * @param oldValue - old value.
 * @param newValue - new value.
 * @param <F> - value type.
 *
 * @author Ihar Smolka
 */
public record ValueDifference<F>(String valueFieldPath,
                                    Class<?> valueFieldRootClass,

                                    Class<?> valueFieldDeclaringClass,
                                    Class<F> valueClass,
                                    F oldValue,
                                    F newValue) implements Difference {

    @Override
    public <T extends Difference> T unwrap(Class<T> type) {
        if (type.isAssignableFrom(ValueDifference.class)) {
            return type.cast(this);
        }

        throw new ClassCastException(String.format("Cannot unwrap AttributeDifference to %s", type));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ValueDifference<?> that = (ValueDifference<?>) o;
        return Objects.equals(valueFieldPath, that.valueFieldPath) && Objects.equals(valueFieldRootClass, that.valueFieldRootClass) && Objects.equals(valueClass, that.valueClass) && Objects.equals(oldValue, that.oldValue) && Objects.equals(newValue, that.newValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(valueFieldPath, valueFieldRootClass, valueClass, oldValue, newValue);
    }
}
