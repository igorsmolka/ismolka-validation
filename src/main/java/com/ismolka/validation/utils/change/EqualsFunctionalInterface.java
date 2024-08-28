package com.ismolka.validation.utils.change;

@FunctionalInterface
public interface EqualsFunctionalInterface<T> {
    boolean test(T t);
}
