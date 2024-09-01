package com.ismolka.validation.utils.change.map;

import com.ismolka.validation.utils.change.Difference;

import java.util.Map;
import java.util.Objects;

public record MapElementDifference<K, V>(
        Map<String, Difference> diffBetweenElementsFields,

        V elementFromOldMap,

        V elementFromNewMap,

        K key
) implements Difference {

    @Override
    public <TYPE extends Difference> TYPE unwrap(Class<TYPE> type) {
        if (type.isAssignableFrom(MapElementDifference.class)) {
            return type.cast(this);
        }

        throw new ClassCastException(String.format("Cannot unwrap MapElementDifference to %s", type));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MapElementDifference<?, ?> that = (MapElementDifference<?, ?>) o;
        return Objects.equals(diffBetweenElementsFields, that.diffBetweenElementsFields) && Objects.equals(elementFromOldMap, that.elementFromOldMap) && Objects.equals(elementFromNewMap, that.elementFromNewMap) && Objects.equals(key, that.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(diffBetweenElementsFields, elementFromOldMap, elementFromNewMap, key);
    }
}
