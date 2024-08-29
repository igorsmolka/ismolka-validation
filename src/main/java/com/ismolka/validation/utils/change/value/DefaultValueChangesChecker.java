package com.ismolka.validation.utils.change.value;

import com.ismolka.validation.utils.change.ChangesChecker;
import com.ismolka.validation.utils.change.collection.CollectionChangesChecker;
import com.ismolka.validation.utils.change.collection.CollectionChangesCheckerResult;
import com.ismolka.validation.utils.change.Difference;
import com.ismolka.validation.utils.metainfo.FieldPath;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.*;
import java.util.function.BiPredicate;
import java.util.stream.Stream;

public class DefaultValueChangesChecker<T> implements ValueChangesChecker<T> {

    protected Set<ValueCheckDescriptor> attributesCheckDescriptors;

    protected boolean stopOnFirstDiff;

    protected Method globalEqualsMethodReflectionRef;

    protected BiPredicate<T, T> globalBiEqualsMethodCodeRef;

    protected final Set<FieldPath> globalEqualsFields;

    protected DefaultValueChangesChecker(Set<ValueCheckDescriptor> attributesCheckDescriptors,
                                         boolean stopOnFirstDiff,
                                         Method globalEqualsMethodReflectionRef,
                                         BiPredicate<T, T> globalBiEqualsMethodCodeRef,
                                         Set<FieldPath> globalEqualsFields) {
        this.attributesCheckDescriptors = attributesCheckDescriptors;
        this.stopOnFirstDiff = stopOnFirstDiff;
        this.globalEqualsMethodReflectionRef = globalEqualsMethodReflectionRef;
        this.globalBiEqualsMethodCodeRef = globalBiEqualsMethodCodeRef;
        this.globalEqualsFields = globalEqualsFields;
    }

    @Override
    public ValueChangesCheckerResult getResult(T oldObj, T newObj) {
        if (oldObj == newObj) {
            return new ValueChangesCheckerResult(new HashMap<>(), true);
        }

        Map<String, Difference> diffMap = new HashMap<>();

        checkByGlobalSettingsAndPutDiffInMap(oldObj, newObj, diffMap);

        return new ValueChangesCheckerResult(diffMap, diffMap.isEmpty());
    }

    private void checkByGlobalSettingsAndPutDiffInMap(T oldObj, T newObj, Map<String, Difference> diffMap) {
        if (oldObj == newObj) {
            return;
        }

        Class<T> sourceClass = (Class<T>) Stream.of(newObj, oldObj).filter(Objects::nonNull).findFirst().get().getClass();

        if (globalEqualsMethodReflectionRef != null) {
            T firstNonNull = Stream.of(newObj, oldObj).filter(Objects::nonNull).findFirst().get();
            T argObj = Stream.of(newObj, oldObj).filter(obj -> obj != firstNonNull).findFirst().get();

            Boolean result = (Boolean) ReflectionUtils.invokeMethod(globalEqualsMethodReflectionRef, firstNonNull, argObj);
            if (result != null && !result) {
                diffMap.put(null, new ValueDifference<>(null, null, null, sourceClass, oldObj, newObj));
            } else {
                return;
            }
        }

        if (globalBiEqualsMethodCodeRef != null) {
            boolean isEquals = globalBiEqualsMethodCodeRef.test(oldObj, newObj);

            if (!isEquals) {
                diffMap.put(null, new ValueDifference<>(null, null, null, sourceClass, oldObj, newObj));
            }

            return;
        }

        if (!CollectionUtils.isEmpty(attributesCheckDescriptors)) {
            for (ValueCheckDescriptor attributeToCheck : attributesCheckDescriptors) {
                Object newAttrVal = attributeToCheck.attribute().getValueFromObject(newObj);
                Object oldAttrVal = attributeToCheck.attribute().getValueFromObject(oldObj);

                checkByAttributeDescriptorAndPutDiffInMap(attributeToCheck, newAttrVal, oldAttrVal, diffMap);

                if (!diffMap.isEmpty() && stopOnFirstDiff) {
                    return;
                }
            }
        }

        if (!CollectionUtils.isEmpty(globalEqualsFields)) {
            for (FieldPath equalsField : globalEqualsFields) {
                Class<?> attributeClass = equalsField.getLast().clazz();
                Class<?> attributeDeclaringClass = equalsField.getLast().declaringClass();
                Object oldObjVal = equalsField.getValueFromObject(oldObj);
                Object newObjVal = equalsField.getValueFromObject(newObj);

                if (!Objects.equals(oldObjVal, newObjVal)) {
                    diffMap.put(equalsField.path(), new ValueDifference<>(equalsField.path(), sourceClass, attributeDeclaringClass, (Class) attributeClass, oldObjVal, newObjVal));
                    if (stopOnFirstDiff) {
                        return;
                    }
                }
            }

            return;
        }

        boolean equalsResult = Objects.equals(oldObj, newObj);
        if (!equalsResult) {
            diffMap.put(null, new ValueDifference<>(null, null, null, sourceClass, oldObj, newObj));
        }
    }

    private void checkByAttributeDescriptorAndPutDiffInMap(ValueCheckDescriptor attributeToCheck, Object newAttrVal, Object oldAttrVal, Map<String, Difference> diffMap) {
        if (newAttrVal == oldAttrVal) {
            return;
        }

        Class<?> fieldRootClass = attributeToCheck.attribute().clazz();
        Class<?> fieldClass = attributeToCheck.attribute().getLast().clazz();
        Class<?> fieldSourceClass = attributeToCheck.attribute().getLast().declaringClass();

        if (newAttrVal == null || oldAttrVal == null) {
            diffMap.put(attributeToCheck.attribute().path(), new ValueDifference(attributeToCheck.attribute().path(), fieldRootClass, fieldSourceClass, fieldClass, oldAttrVal, newAttrVal));
            return;
        }

        ChangesChecker changesChecker = attributeToCheck.changesChecker();

        if (changesChecker != null) {
            if (CollectionChangesChecker.class.isAssignableFrom(changesChecker.getClass())) {
                if (!Collection.class.isAssignableFrom(newAttrVal.getClass()) || !Collection.class.isAssignableFrom(oldAttrVal.getClass())) {
                    throw new RuntimeException("One of objects is not a collection");
                }

                CollectionChangesChecker<?> collectionChangesChecker = (CollectionChangesChecker<?>) changesChecker;
                CollectionChangesCheckerResult<?> collectionChangesCheckerResult = collectionChangesChecker.getResult((Collection) oldAttrVal, (Collection) newAttrVal);
                diffMap.put(attributeToCheck.attribute().path(), collectionChangesCheckerResult);
                return;
            }

            if (ValueChangesChecker.class.isAssignableFrom(changesChecker.getClass())) {
                ValueChangesChecker valueChangesChecker = (ValueChangesChecker<?>) changesChecker;
                ValueChangesCheckerResult valueChangesCheckerResult = valueChangesChecker.getResult(oldAttrVal, newAttrVal);
                diffMap.put(attributeToCheck.attribute().path(), valueChangesCheckerResult);
                return;
            }
        }


        if (attributeToCheck.equalsMethodReflectionRef() != null) {
            Boolean result = (Boolean) ReflectionUtils.invokeMethod(attributeToCheck.equalsMethodReflectionRef(), oldAttrVal, newAttrVal);

            if (result != null && result) {
                diffMap.put(attributeToCheck.attribute().path(), new ValueDifference(attributeToCheck.attribute().path(), fieldRootClass, fieldSourceClass, fieldClass, oldAttrVal, newAttrVal));
            }

            return;
        }

        if (!CollectionUtils.isEmpty(attributeToCheck.equalsFields())) {
            for (FieldPath equalsField : attributeToCheck.equalsFields()) {
                Class<?> attributeClass = equalsField.getLast().clazz();
                Class<?> attributeDeclaringClass = equalsField.getLast().declaringClass();
                Object oldObjVal = equalsField.getValueFromObject(oldAttrVal);
                Object newObjVal = equalsField.getValueFromObject(newAttrVal);

                if (!Objects.equals(oldObjVal, newObjVal)) {
                    diffMap.put(attributeToCheck.attribute().path(), new ValueDifference(equalsField.path(), fieldRootClass, attributeDeclaringClass,  attributeClass, oldObjVal, newObjVal));
                }
            }

            return;
        }

        if (attributeToCheck.biEqualsMethodCodeRef() != null) {
            BiPredicate biPredicate = attributeToCheck.biEqualsMethodCodeRef();
            boolean biEqualsResult = biPredicate.test(oldAttrVal, newAttrVal);
            if (!biEqualsResult) {
                diffMap.put(attributeToCheck.attribute().path(), new ValueDifference(attributeToCheck.attribute().path(), fieldRootClass, fieldSourceClass, fieldClass, oldAttrVal, newAttrVal));
            }
        }

        boolean equalsResult = Objects.equals(newAttrVal, oldAttrVal);
        if (!equalsResult) {
            diffMap.put(attributeToCheck.attribute().path(), new ValueDifference(attributeToCheck.attribute().path(), fieldRootClass, fieldSourceClass, fieldClass, oldAttrVal, newAttrVal));
        }
    }
}
