package com.ismolka.validation.utils.change.attribute;

import org.antlr.v4.runtime.misc.OrderedHashSet;

import java.util.Set;

public class DefaultAttributeChangesCheckerBuilder<T> {

    Class<T> clazz;

    Set<AttributeMetaInfo> attributesToCheck;

    boolean stopOnFirstDiff;

    public static <T> DefaultAttributeChangesCheckerBuilder<T> builder(Class<T> clazz) {
        return new DefaultAttributeChangesCheckerBuilder<>(clazz);
    }

    private DefaultAttributeChangesCheckerBuilder(Class<T> clazz) {
        this.clazz = clazz;
    }

    public DefaultAttributeChangesCheckerBuilder<T> addAttributeToCheck(AttributeMetaInfo attribute) {
        if (attributesToCheck == null) {
            attributesToCheck = new OrderedHashSet<>();
        }

        attributesToCheck.add(attribute);

        return this;
    }

    public DefaultAttributeChangesCheckerBuilder<T> stopOnFirstDiff() {
        this.stopOnFirstDiff = true;

        return this;
    }

    public DefaultAttributeChangesChecker<T> build() {
        return new DefaultAttributeChangesChecker<>(attributesToCheck, stopOnFirstDiff);
    }
}
