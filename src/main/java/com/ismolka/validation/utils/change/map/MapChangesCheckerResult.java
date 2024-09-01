package com.ismolka.validation.utils.change.map;

import com.ismolka.validation.utils.change.CheckerResult;
import com.ismolka.validation.utils.change.Difference;
import com.ismolka.validation.utils.change.navigator.CheckerResultNavigator;
import com.ismolka.validation.utils.change.navigator.DefaultCheckerResultNavigator;
import com.ismolka.validation.utils.constant.MapOperation;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

public record MapChangesCheckerResult<K, V>(
        Class<K> keyClass,

        Class<V> valueClass,

        Map<MapOperation, Set<MapElementDifference<K, V>>> mapDifference,

        boolean equalsResult
) implements Difference, CheckerResult {

    @Override
    public <TYPE extends Difference> TYPE unwrap(Class<TYPE> type) {
        if (type.isAssignableFrom(MapChangesCheckerResult.class)) {
            return type.cast(this);
        }

        throw new ClassCastException(String.format("Cannot unwrap AttributeDifference to %s", type));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MapChangesCheckerResult<?, ?> that = (MapChangesCheckerResult<?, ?>) o;
        return equalsResult == that.equalsResult && Objects.equals(keyClass, that.keyClass) && Objects.equals(valueClass, that.valueClass) && Objects.equals(mapDifference, that.mapDifference);
    }

    @Override
    public int hashCode() {
        return Objects.hash(keyClass, valueClass, mapDifference, equalsResult);
    }

    @Override
    public CheckerResultNavigator navigator() {
        return new DefaultCheckerResultNavigator(this);
    }
}
