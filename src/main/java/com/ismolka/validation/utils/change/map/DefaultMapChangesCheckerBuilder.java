package com.ismolka.validation.utils.change.map;

import com.ismolka.validation.utils.change.collection.DefaultCollectionChangesCheckerBuilder;
import com.ismolka.validation.utils.change.value.ValueCheckDescriptor;
import com.ismolka.validation.utils.constant.CollectionOperation;
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

public class DefaultMapChangesCheckerBuilder<K, V> {

    private Class<K> keyClass;

    private Class<V> valueClass;

    private Set<MapOperation> forOperations;

    Set<ValueCheckDescriptor<?>> attributesCheckDescriptors;

    boolean stopOnFirstDiff;

    Method globalEqualsMethodReflectionRef;

    BiPredicate<V, V> globalBiEqualsMethodCodeRef;

    Set<String> globalEqualsFields;


    public static <K, V> DefaultMapChangesCheckerBuilder<K, V> builder(Class<K> keyClass, Class<V> valueClass) {
        return new DefaultMapChangesCheckerBuilder<>(keyClass, valueClass);
    }

    public DefaultMapChangesCheckerBuilder(Class<K> keyClass, Class<V> valueClass) {
        this.keyClass = keyClass;
        this.valueClass = valueClass;
    }

    public DefaultMapChangesCheckerBuilder<K, V> addAttributeToCheck(ValueCheckDescriptor<?> attribute) {
        if (attributesCheckDescriptors == null) {
            attributesCheckDescriptors = new OrderedHashSet<>();
        }

        attributesCheckDescriptors.add(attribute);

        return this;
    }

    public DefaultMapChangesCheckerBuilder<K, V> stopOnFirstDiff() {
        this.stopOnFirstDiff = true;

        return this;
    }

    public DefaultMapChangesCheckerBuilder<K, V> globalEqualsMethodReflectionRef(Method globalEqualsMethodReflectionRef) {
        this.globalEqualsMethodReflectionRef = globalEqualsMethodReflectionRef;

        return this;
    }

    public DefaultMapChangesCheckerBuilder<K, V> globalBiEqualsMethodCodeRef(BiPredicate<V, V> globalBiEqualsMethodCodeRef) {
        this.globalBiEqualsMethodCodeRef = globalBiEqualsMethodCodeRef;

        return this;
    }

    public DefaultMapChangesCheckerBuilder<K, V> addGlobalEqualsField(String globalEqualsField) {
        if (globalEqualsFields == null) {
            globalEqualsFields = new OrderedHashSet<>();
        }

        globalEqualsFields.add(globalEqualsField);

        return this;
    }

    public DefaultMapChangesCheckerBuilder<K, V> forOperation(MapOperation operation) {
        if (forOperations == null) {
            forOperations = new HashSet<>();
        }

        forOperations.add(operation);

        return this;
    }

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

        return new DefaultMapChangesChecker<>(attributesCheckDescriptors, stopOnFirstDiff, globalEqualsMethodReflectionRef, globalBiEqualsMethodCodeRef, globalEqualsFieldsAsFieldPaths, keyClass, valueClass, forOperations);
    }

    private void validate() {
        if (keyClass == null) {
            throw new RuntimeException("Key class is not defined");
        }

        if (valueClass == null) {
            throw new RuntimeException("Value class is not defined");
        }

        if (!CollectionUtils.isEmpty(attributesCheckDescriptors) || !CollectionUtils.isEmpty(globalEqualsFields)) {
            if (globalBiEqualsMethodCodeRef != null || globalEqualsMethodReflectionRef != null) {
                throw new RuntimeException("Cannot set global equals method when attribute check descriptors or equals fields are defined");
            }
        }

        if (globalBiEqualsMethodCodeRef != null && globalEqualsMethodReflectionRef != null) {
            throw new RuntimeException("Should be only one kind of defining global equals method for map check");
        }

        if (globalEqualsMethodReflectionRef != null && ReflectUtil.methodIsNotPresent(globalEqualsMethodReflectionRef, valueClass)) {
            throw new RuntimeException(String.format("Value class %s doesnt declare the method %s", valueClass, globalEqualsMethodReflectionRef));
        }

        if (globalEqualsFields != null) {
            globalEqualsFields.forEach(equalsField -> {
                if (ReflectUtil.fieldPathIsNotPresent(equalsField, valueClass)) {
                    throw new RuntimeException(String.format("Equals field %s is not present in map value class %s", equalsField, valueClass));
                }
            });
        }
    }
}
