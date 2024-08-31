package com.ismolka.validation.utils.change.navigator;

import com.ismolka.validation.utils.change.Difference;
import com.ismolka.validation.utils.change.value.ValueDifference;

import java.util.List;

public interface CheckerResultNavigator {

    boolean hasDiff();

    boolean hasDiff(String fieldPath);

    <T> ValueDifference<T> getSingleDiff(Class<T> forClass);

    <T> ValueDifference<T> getSingleDiff(String fieldPath, Class<T> forClass);

    <T, DIFF extends Difference> List<DIFF> getAllDiffs(Class<T> forClass, Class<DIFF> diffClass);

    <T, DIFF extends Difference> List<DIFF> getAllDiffs(String fieldPath, Class<T> forClass, Class<DIFF> diffClass);
}
