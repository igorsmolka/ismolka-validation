package com.ismolka.validation.utils.change;

public interface Difference {

    <TYPE extends Difference> TYPE unwrap(Class<TYPE> type);
}
