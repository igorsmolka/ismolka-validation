package com.ismolka.validation.utils.change.navigator;

import com.ismolka.validation.utils.change.Difference;
import com.ismolka.validation.utils.change.value.ValueDifference;

import java.util.List;

public interface CheckerResultNavigator {
    Difference getDifference(String fieldPath);

}
