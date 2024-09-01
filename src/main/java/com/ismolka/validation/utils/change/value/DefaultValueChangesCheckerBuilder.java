package com.ismolka.validation.utils.change.value;

import com.ismolka.validation.utils.metainfo.FieldPath;
import com.ismolka.validation.utils.metainfo.MetaInfoExtractorUtil;
import com.ismolka.validation.utils.reflection.ReflectUtil;
import org.antlr.v4.runtime.misc.OrderedHashSet;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Method;
import java.util.Set;
import java.util.function.BiPredicate;

/**
 * Builder for {@link ValueCheckDescriptor}.
 * @see DefaultValueChangesChecker
 *
 * @param <T> - value type
 * @author Ihar Smolka
 */
public class DefaultValueChangesCheckerBuilder<T> {

    Class<T> targetClass;

    Set<ValueCheckDescriptor<?>> attributesCheckDescriptors;

    boolean stopOnFirstDiff;

    Method globalEqualsMethodReflection;

    BiPredicate<T, T> globalBiEqualsMethod;

    Set<String> globalEqualsFields;

    /**
     * Static method for builder instantiation.
     *
     * @param clazz - class of the attribute
     * @return builder
     * @param <T> - value type
     */
    public static <T> DefaultValueChangesCheckerBuilder<T> builder(Class<T> clazz) {
        return new DefaultValueChangesCheckerBuilder<>(clazz);
    }

    private DefaultValueChangesCheckerBuilder(Class<T> targetClass) {
        this.targetClass = targetClass;
    }

    /**
     * {@link ValueCheckDescriptor} for attribute check.
     *
     * @param attribute - descriptor
     * @return this
     */
    public DefaultValueChangesCheckerBuilder<T> addAttributeToCheck(ValueCheckDescriptor<?> attribute) {
        if (attributesCheckDescriptors == null) {
            attributesCheckDescriptors = new OrderedHashSet<>();
        }

        attributesCheckDescriptors.add(attribute);

        return this;
    }

    /**
     * Flag to stop finding on first diff
     * @return this
     */
    public DefaultValueChangesCheckerBuilder<T> stopOnFirstDiff() {
        this.stopOnFirstDiff = true;

        return this;
    }

    /**
     * Equals method
     *
     * @param globalEqualsMethodReflectionRef - {@link Method}
     * @return this
     */
    public DefaultValueChangesCheckerBuilder<T> globalEqualsMethodReflection(Method globalEqualsMethodReflectionRef) {
        this.globalEqualsMethodReflection = globalEqualsMethodReflectionRef;

        return this;
    }

    /**
     * Equals method with two arguments
     *
     * @param globalBiEqualsMethodCodeRef - {@link BiPredicate}
     * @return this
     */
    public DefaultValueChangesCheckerBuilder<T> globalBiEqualsMethod(BiPredicate<T, T> globalBiEqualsMethodCodeRef) {
        this.globalBiEqualsMethod = globalBiEqualsMethodCodeRef;

        return this;
    }

    /**
     * Equals field
     *
     * @param globalEqualsField - field for equals
     * @return this
     */
    public DefaultValueChangesCheckerBuilder<T> addGlobalEqualsField(String globalEqualsField) {
        if (globalEqualsFields == null) {
            globalEqualsFields = new OrderedHashSet<>();
        }

        globalEqualsFields.add(globalEqualsField);

        return this;
    }

    public DefaultValueChangesChecker<T> build() {
        validate();

        Set<FieldPath> equalsFieldsAsFieldPaths = !CollectionUtils.isEmpty(globalEqualsFields) ? MetaInfoExtractorUtil.extractFieldPathsMetaInfo(globalEqualsFields.toArray(String[]::new), targetClass) : new OrderedHashSet<>();

        return new DefaultValueChangesChecker<>(attributesCheckDescriptors, stopOnFirstDiff, globalEqualsMethodReflection, globalBiEqualsMethod, equalsFieldsAsFieldPaths);
    }

    private void validate() {
        if (targetClass == null) {
            throw new IllegalArgumentException("Target class is not defined");
        }

        if (!CollectionUtils.isEmpty(attributesCheckDescriptors) || !CollectionUtils.isEmpty(globalEqualsFields)) {
            if (globalBiEqualsMethod != null || globalEqualsMethodReflection != null) {
                throw new IllegalArgumentException("Cannot set global equals method when attribute check descriptors or equals fields are defined");
            }
        }

        if (globalBiEqualsMethod != null && globalEqualsMethodReflection != null) {
            throw new IllegalArgumentException("Should be only one kind of defining global equals method for value check");
        }

        if (globalEqualsMethodReflection != null) {
            if (ReflectUtil.methodIsNotPresent(globalEqualsMethodReflection, targetClass)) {
                throw new IllegalArgumentException(String.format("Target class %s doesnt declare the method %s", targetClass, globalEqualsMethodReflection));
            }

            if (!globalEqualsMethodReflection.getReturnType().equals(boolean.class) && !globalEqualsMethodReflection.getReturnType().equals(Boolean.class)) {
                throw new IllegalArgumentException("Equals method must return boolean");
            }
        }

        if (globalEqualsFields != null) {
            globalEqualsFields.forEach(equalsField -> {
                if (ReflectUtil.fieldPathIsNotPresent(equalsField, targetClass)) {
                    throw new IllegalArgumentException(String.format("Equals field %s is not present in class %s", equalsField, targetClass));
                }
            });
        }
    }
}
