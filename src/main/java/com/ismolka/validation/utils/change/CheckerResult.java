package com.ismolka.validation.utils.change;

import com.ismolka.validation.utils.change.navigator.CheckerResultNavigator;

/**
 * Interface for check result.
 *
 * @author Ihar Smolka
 */
public interface CheckerResult extends Difference {

    /**
     * @return {@link CheckerResultNavigator}
     */
    CheckerResultNavigator navigator();

    /**
     * @return equals result
     */
    boolean equalsResult();
}
