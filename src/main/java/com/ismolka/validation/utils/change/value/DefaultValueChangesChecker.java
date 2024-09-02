package com.ismolka.validation.utils.change.value;

import com.ismolka.validation.utils.change.ChangesChecker;
import com.ismolka.validation.utils.change.collection.CollectionChangesChecker;
import com.ismolka.validation.utils.change.collection.CollectionChangesCheckerResult;
import com.ismolka.validation.utils.change.Difference;
import com.ismolka.validation.utils.change.map.MapChangesChecker;
import com.ismolka.validation.utils.change.map.MapChangesCheckerResult;
import com.ismolka.validation.utils.metainfo.FieldPath;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.*;
import java.util.function.BiPredicate;
import java.util.stream.Stream;

public class DefaultValueChangesChecker<T> implements ValueChangesChecker<T> {

    protected Set<ValueCheckDescriptor<?>> attributesCheckDescriptors;

    protected boolean stopOnFirstDiff;

    protected Method globalEqualsMethodReflection;

    protected BiPredicate<T, T> globalBiEqualsMethod;

    protected final Set<FieldPath> globalEqualsFields;

    protected DefaultValueChangesChecker(Set<ValueCheckDescriptor<?>> attributesCheckDescriptors,
                                         boolean stopOnFirstDiff,
                                         Method globalEqualsMethodReflection,
                                         BiPredicate<T, T> globalBiEqualsMethod,
                                         Set<FieldPath> globalEqualsFields) {
        this.attributesCheckDescriptors = attributesCheckDescriptors;
        this.stopOnFirstDiff = stopOnFirstDiff;
        this.globalEqualsMethodReflection = globalEqualsMethodReflection;
        this.globalBiEqualsMethod = globalBiEqualsMethod;
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

    @SuppressWarnings("uncheked")
    private void checkByGlobalSettingsAndPutDiffInMap(T oldObj, T newObj, Map<String, Difference> diffMap) {
        if (oldObj == newObj) {
            return;
        }

        Class<T> sourceClass = (Class<T>) Stream.of(newObj, oldObj).filter(Objects::nonNull).findFirst().get().getClass();

        if (globalEqualsMethodReflection != null) {
            T firstNonNull = Stream.of(newObj, oldObj).filter(Objects::nonNull).findFirst().get();
            T argObj = Stream.of(newObj, oldObj).filter(obj -> obj != firstNonNull).findFirst().get();

            Boolean result = (Boolean) ReflectionUtils.invokeMethod(globalEqualsMethodReflection, firstNonNull, argObj);
            if (result != null && !result) {
                diffMap.put(null, new ValueDifference<>(null, null, null, sourceClass, oldObj, newObj));
            } else {
                return;
            }
        }

        if (globalBiEqualsMethod != null) {
            boolean isEquals = globalBiEqualsMethod.test(oldObj, newObj);

            if (!isEquals) {
                diffMap.put(null, new ValueDifference<>(null, null, null, sourceClass, oldObj, newObj));
            }

            return;
        }

        if (!CollectionUtils.isEmpty(attributesCheckDescriptors) || !CollectionUtils.isEmpty(globalEqualsFields)) {
            if (!CollectionUtils.isEmpty(attributesCheckDescriptors)) {
                for (ValueCheckDescriptor<?> attributeToCheck : attributesCheckDescriptors) {
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
                        diffMap.put(equalsField.path(), differenceToReferenceChainByPath(equalsField.path(), new ValueDifference<>(equalsField.path(), sourceClass, attributeDeclaringClass, (Class<Object>) attributeClass, oldObjVal, newObjVal)));
                        if (stopOnFirstDiff) {
                            return;
                        }
                    }
                }
            }
        } else {
            boolean equalsResult = Objects.equals(oldObj, newObj);
            if (!equalsResult) {
                diffMap.put(null, new ValueDifference<>(null, null, null, sourceClass, oldObj, newObj));
            }
        }
    }

    @SuppressWarnings("unchecked")
    private <X> void checkByAttributeDescriptorAndPutDiffInMap(ValueCheckDescriptor<X> attributeToCheck, Object newAttrVal, Object oldAttrVal, Map<String, Difference> diffMap) {
        if (newAttrVal == oldAttrVal) {
            return;
        }

        Class<?> fieldRootClass = attributeToCheck.attribute().clazz();
        Class<?> fieldClass = attributeToCheck.attribute().getLast().clazz();
        Class<?> fieldSourceClass = attributeToCheck.attribute().getLast().declaringClass();

        if (newAttrVal == null || oldAttrVal == null) {
            diffMap.put(attributeToCheck.attribute().path(), differenceToReferenceChainByPath(attributeToCheck.attribute().path(), new ValueDifference<>(attributeToCheck.attribute().path(), fieldRootClass, fieldSourceClass, (Class<X>) fieldClass, (X) oldAttrVal, (X) newAttrVal)));
            return;
        }

        ChangesChecker<X> changesChecker = attributeToCheck.changesChecker();

        if (changesChecker != null) {
            if (CollectionChangesChecker.class.isAssignableFrom(changesChecker.getClass())) {
                if (!Collection.class.isAssignableFrom(newAttrVal.getClass()) || !Collection.class.isAssignableFrom(oldAttrVal.getClass())) {
                    throw new IllegalArgumentException("One of objects is not a Collection");
                }

                CollectionChangesChecker<X> collectionChangesChecker = (CollectionChangesChecker<X>) changesChecker;
                CollectionChangesCheckerResult<X> collectionChangesCheckerResult = collectionChangesChecker.getResult((Collection<X>) oldAttrVal, (Collection<X>) newAttrVal);
                diffMap.put(attributeToCheck.attribute().path(), collectionChangesCheckerResult);
                return;
            }

            if (MapChangesChecker.class.isAssignableFrom(changesChecker.getClass())) {
                if (!Map.class.isAssignableFrom(newAttrVal.getClass()) || !Map.class.isAssignableFrom(oldAttrVal.getClass())) {
                    throw new IllegalArgumentException("One of objects is not a Map");
                }
                MapChangesChecker<?, X> mapChangesChecker = (MapChangesChecker<?, X>) changesChecker;
                MapChangesCheckerResult<?, X> mapChangesCheckerResult = mapChangesChecker.getResult((Map) oldAttrVal, (Map) newAttrVal);
                diffMap.put(attributeToCheck.attribute().path(), mapChangesCheckerResult);
                return;
            }

            if (ValueChangesChecker.class.isAssignableFrom(changesChecker.getClass())) {
                ValueChangesChecker<X> valueChangesChecker = (ValueChangesChecker<X>) changesChecker;
                ValueChangesCheckerResult valueChangesCheckerResult = valueChangesChecker.getResult((X) oldAttrVal, (X) newAttrVal);
                diffMap.put(attributeToCheck.attribute().path(), valueChangesCheckerResult);
                return;
            }
        }


        if (attributeToCheck.equalsMethodReflection() != null) {
            Boolean result = (Boolean) ReflectionUtils.invokeMethod(attributeToCheck.equalsMethodReflection(), oldAttrVal, newAttrVal);

            if (result != null && result) {
                diffMap.put(attributeToCheck.attribute().path(), differenceToReferenceChainByPath(attributeToCheck.attribute().path(), new ValueDifference<>(attributeToCheck.attribute().path(), fieldRootClass, fieldSourceClass, (Class<X>) fieldClass, (X) oldAttrVal, (X) newAttrVal)));
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
                    diffMap.put(attributeToCheck.attribute().path(), differenceToReferenceChainByPath(equalsField.path(), new ValueDifference<>(equalsField.path(), fieldRootClass, attributeDeclaringClass, (Class<Object>) attributeClass,  oldObjVal, newObjVal)));
                }
            }

            return;
        }

        if (attributeToCheck.biEqualsMethod() != null) {
            BiPredicate<X, X> biPredicate = attributeToCheck.biEqualsMethod();
            boolean biEqualsResult = biPredicate.test((X) oldAttrVal, (X) newAttrVal);
            if (!biEqualsResult) {
                diffMap.put(attributeToCheck.attribute().path(), differenceToReferenceChainByPath(attributeToCheck.attribute().path(), new ValueDifference<>(attributeToCheck.attribute().path(), fieldRootClass, fieldSourceClass, (Class<X>) fieldClass, (X) oldAttrVal, (X) newAttrVal)));
            }
        }

        boolean equalsResult = Objects.equals(newAttrVal, oldAttrVal);
        if (!equalsResult) {
            diffMap.put(attributeToCheck.attribute().path(), differenceToReferenceChainByPath(attributeToCheck.attribute().path(), new ValueDifference<>(attributeToCheck.attribute().path(), fieldRootClass, fieldSourceClass, (Class<X>) fieldClass, (X) oldAttrVal, (X) newAttrVal)));
        }
    }

    protected DifferenceRef differenceToReferenceChainByPath(String path, Difference difference) {
        String[] parts = path.split("\\.");

        Difference onDiff = difference;
        DifferenceRef differenceRef = null;

        for (int i = parts.length - 1; i >= 0; i--) {
            differenceRef = new DifferenceRef(parts[i], onDiff);
            onDiff = differenceRef;
        }

        return differenceRef;
    }
}
