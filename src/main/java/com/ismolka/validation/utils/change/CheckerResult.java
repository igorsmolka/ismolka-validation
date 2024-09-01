package com.ismolka.validation.utils.change;

import com.ismolka.validation.utils.change.navigator.CheckerResultNavigator;

public interface CheckerResult extends Difference {

    CheckerResultNavigator navigator();
    boolean equalsResult();
}
