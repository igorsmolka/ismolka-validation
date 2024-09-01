package com.ismolka.validation.utils.change.value;

import com.ismolka.validation.utils.change.Difference;

import java.util.Objects;

/**
 * Reference to difference.
 * @see Difference
 *
 * @param onField - reference for attribute
 * @param toDifference - {@link Difference}
 *
 * @author Ihar Smolka
 */
public record DifferenceRef(
        String onField,
        Difference toDifference
) implements Difference {


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DifferenceRef that = (DifferenceRef) o;
        return Objects.equals(toDifference, that.toDifference);
    }

    @Override
    public int hashCode() {
        return Objects.hash(toDifference);
    }

    @Override
    public <TYPE extends Difference> TYPE unwrap(Class<TYPE> type) {
        if (type.isAssignableFrom(DifferenceRef.class)) {
            return type.cast(this);
        }

        throw new ClassCastException(String.format("Cannot unwrap DifferenceRef to %s", type));
    }
}
