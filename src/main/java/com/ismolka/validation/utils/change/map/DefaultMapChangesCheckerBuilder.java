package com.ismolka.validation.utils.change.map;

import com.ismolka.validation.utils.change.value.ValueCheckDescriptor;
import com.ismolka.validation.utils.constant.MapOperation;
import com.ismolka.validation.utils.metainfo.FieldPath;
import com.ismolka.validation.utils.metainfo.MetaInfoExtractorUtil;
import com.ismolka.validation.utils.reflection.ReflectUtil;
import org.antlr.v4.runtime.misc.OrderedHashSet;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiPredicate;

/**
 * Builder for {@link DefaultMapChangesChecker}
 *
 * @param <K> - key type
 * @param <V> - value type
 */
public class DefaultMapChangesCheckerBuilder<K, V> {

    Class<K> keyClass;

    Class<V> valueClass;

    Set<MapOperation> forOperations;

    Set<ValueCheckDescriptor<?>> attributesCheckDescriptors;

    boolean stopOnFirstDiff;

    Method globalEqualsMethodReflection;

    BiPredicate<V, V> globalBiEqualsMethod;

    Set<String> globalEqualsFields;


    /**
     * Static method for builder instantiation.
     *
     * @param keyClass - key class
     * @param valueClass - value class
     * @return builder
     * @param <K> - key type
     * @param <V> - value type
     */
    public static <K, V> DefaultMapChangesCheckerBuilder<K, V> builder(Class<K> keyClass, Class<V> valueClass) {
        return new DefaultMapChangesCheckerBuilder<>(keyClass, valueClass);
    }

    private DefaultMapChangesCheckerBuilder(Class<K> keyClass, Class<V> valueClass) {
        this.keyClass = keyClass;
        this.valueClass = valueClass;
    }

    /**
     * {@link ValueCheckDescriptor} for attribute check.
     *
     * @param attribute - descriptor
     * @return this
     */
    public DefaultMapChangesCheckerBuilder<K, V> addAttributeToCheck(ValueCheckDescriptor<?> attribute) {
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
    public DefaultMapChangesCheckerBuilder<K, V> stopOnFirstDiff() {
        this.stopOnFirstDiff = true;

        return this;
    }

    /**
     * Equals method
     *
     * @param globalEqualsMethodReflectionRef - {@link Method}
     * @return this
     */
    public DefaultMapChangesCheckerBuilder<K, V> globalEqualsMethodReflection(Method globalEqualsMethodReflectionRef) {
        this.globalEqualsMethodReflection = globalEqualsMethodReflectionRef;

        return this;
    }

    /**
     * Equals method with two arguments
     *
     * @param globalBiEqualsMethodCodeRef - {@link BiPredicate}
     * @return this
     */
    public DefaultMapChangesCheckerBuilder<K, V> globalBiEqualsMethod(BiPredicate<V, V> globalBiEqualsMethodCodeRef) {
        this.globalBiEqualsMethod = globalBiEqualsMethodCodeRef;

        return this;
    }

    /**
     * Equals field
     *
     * @param globalEqualsField - field for equals
     * @return this
     */
    public DefaultMapChangesCheckerBuilder<K, V> addGlobalEqualsField(String globalEqualsField) {
        if (globalEqualsFields == null) {
            globalEqualsFields = new OrderedHashSet<>();
        }

        globalEqualsFields.add(globalEqualsField);

        return this;
    }

    /**
     * Check for {@link MapOperation}
     *
     * @param operation - {@link MapOperation}
     * @return this
     */
    public DefaultMapChangesCheckerBuilder<K, V> forOperation(MapOperation operation) {
        if (forOperations == null) {
            forOperations = new HashSet<>();
        }

        forOperations.add(operation);

        return this;
    }

    /**
     * Check for {@link MapOperation}
     *
     * @param operations - {@link MapOperation}
     * @return this
     */
    public DefaultMapChangesCheckerBuilder<K, V> forOperations(MapOperation... operations) {
        if (forOperations == null) {
            forOperations = new HashSet<>();
        }

        forOperations.addAll(Arrays.stream(operations).toList());

        return this;
    }

    public DefaultMapChangesChecker<K, V> build() {
        if (CollectionUtils.isEmpty(forOperations)) {
            forOperations = new HashSet<>(Arrays.stream(MapOperation.values()).toList());
        }

        validate();

        Set<FieldPath> globalEqualsFieldsAsFieldPaths = !CollectionUtils.isEmpty(globalEqualsFields) ? MetaInfoExtractorUtil.extractFieldPathsMetaInfo(globalEqualsFields.toArray(String[]::new), valueClass) : new OrderedHashSet<>();

        return new DefaultMapChangesChecker<>(attributesCheckDescriptors, stopOnFirstDiff, globalEqualsMethodReflection, globalBiEqualsMethod, globalEqualsFieldsAsFieldPaths, keyClass, valueClass, forOperations);
    }

    private void validate() {
        if (keyClass == null) {
            throw new IllegalArgumentException("Key class is not defined");
        }

        if (valueClass == null) {
            throw new IllegalArgumentException("Value class is not defined");
        }

        if (!CollectionUtils.isEmpty(attributesCheckDescriptors) || !CollectionUtils.isEmpty(globalEqualsFields)) {
            if (globalBiEqualsMethod != null || globalEqualsMethodReflection != null) {
                throw new IllegalArgumentException("Cannot set global equals method when attribute check descriptors or equals fields are defined");
            }
        }

        if (globalBiEqualsMethod != null && globalEqualsMethodReflection != null) {
            throw new IllegalArgumentException("Should be only one kind of defining global equals method for map check");
        }

        if (globalEqualsMethodReflection != null) {
            if (ReflectUtil.methodIsNotPresent(globalEqualsMethodReflection, valueClass)) {
                throw new IllegalArgumentException(String.format("Value class %s doesnt declare the method %s", valueClass, globalEqualsMethodReflection));
            }

            if (!globalEqualsMethodReflection.getReturnType().equals(boolean.class) && !globalEqualsMethodReflection.getReturnType().equals(Boolean.class)) {
                throw new IllegalArgumentException("Equals method must return boolean");
            }
        }

        if (globalEqualsFields != null) {
            globalEqualsFields.forEach(equalsField -> {
                if (ReflectUtil.fieldPathIsNotPresent(equalsField, valueClass)) {
                    throw new IllegalArgumentException(String.format("Equals field %s is not present in map value class %s", equalsField, valueClass));
                }
            });
        }
    }
}
