package com.ismolka.validation.utils.change.value;

import com.ismolka.validation.utils.change.CheckerResult;
import com.ismolka.validation.utils.change.Difference;
import com.ismolka.validation.utils.change.navigator.CheckerResultNavigator;
import com.ismolka.validation.utils.change.navigator.DefaultCheckerResultNavigator;

import java.util.Map;
import java.util.Objects;

public record ValueChangesCheckerResult(
        Map<String, Difference> differenceMap,
        boolean equalsResult
) implements Difference, CheckerResult {

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ValueChangesCheckerResult that = (ValueChangesCheckerResult) o;
        return equalsResult == that.equalsResult && Objects.equals(differenceMap, that.differenceMap);
    }

    @Override
    public int hashCode() {
        return Objects.hash(differenceMap, equalsResult);
    }

    @Override
    public <T extends Difference> T unwrap(Class<T> type) {
        if (type.isAssignableFrom(ValueChangesCheckerResult.class)) {
            return type.cast(this);
        }

        throw new ClassCastException(String.format("Cannot unwrap ValueChangesCheckerResult to %s", type));
    }

    @Override
    public CheckerResultNavigator navigator() {
        return new DefaultCheckerResultNavigator(this);
    }
}
