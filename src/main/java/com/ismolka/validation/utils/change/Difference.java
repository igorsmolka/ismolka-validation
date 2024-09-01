package com.ismolka.validation.utils.change;

/**
 * Difference interface
 *
 * @author Ihar Smolka
 */
public interface Difference {

    /**
     * for unwrapping a difference
     *
     * @param type - toType
     * @return unwrapped difference
     * @param <TYPE> - type
     */
    <TYPE extends Difference> TYPE unwrap(Class<TYPE> type);
}
