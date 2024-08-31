package com.ismolka.validation.utils.change.navigator;

import com.ismolka.validation.utils.change.CheckerResult;
import com.ismolka.validation.utils.change.Difference;
import com.ismolka.validation.utils.change.value.ValueDifference;

import java.util.List;

public class DefaultCheckerResultNavigator implements CheckerResultNavigator {

    private final CheckerResult checkerResult;

    public DefaultCheckerResultNavigator(CheckerResult checkerResult) {
        this.checkerResult = checkerResult;
    }

    @Override
    public boolean hasDiff() {
        return !checkerResult.equalsResult();
    }

    @Override
    public boolean hasDiff(String fieldPath) {
        return false;
    }

    @Override
    public <T> ValueDifference<T> getSingleDiff(Class<T> forClass) {
        return null;
    }

    @Override
    public <T> ValueDifference<T> getSingleDiff(String fieldPath, Class<T> forClass) {
        return null;
    }

    @Override
    public <T, DIFF extends Difference> List<DIFF> getAllDiffs(Class<T> forClass, Class<DIFF> diffClass) {
        return null;
    }

    @Override
    public <T, DIFF extends Difference> List<DIFF> getAllDiffs(String fieldPath, Class<T> forClass, Class<DIFF> diffClass) {
        return null;
    }
}
