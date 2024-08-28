package com.ismolka.validation.utils.change;

public interface Difference {

    <T extends Difference> T unwrap(Class<T> type);
}
