package com.ismolka.validation.utils.change.attribute;

import com.ismolka.validation.utils.change.ChangesChecker;
import com.ismolka.validation.utils.change.collection.CollectionChangesChecker;
import com.ismolka.validation.utils.change.collection.CollectionChangesCheckerResult;
import com.ismolka.validation.utils.change.Difference;
import com.ismolka.validation.validator.metainfo.FieldPath;
import org.antlr.v4.runtime.misc.OrderedHashSet;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ReflectionUtils;

import java.util.*;
import java.util.stream.Stream;

public class DefaultAttributeChangesChecker<T> implements AttributeChangesChecker<T> {

    protected Set<AttributeMetaInfo> attributesToCheck;

    protected boolean stopOnFirstDiff;

    public DefaultAttributeChangesChecker() {
        this.attributesToCheck = new OrderedHashSet<>();
        this.stopOnFirstDiff = false;
    }

    public DefaultAttributeChangesChecker(Set<AttributeMetaInfo> attributesToCheck, boolean stopOnFirstDiff) {
        this.attributesToCheck = attributesToCheck;
        this.stopOnFirstDiff = stopOnFirstDiff;
    }

    public static Builder<?> builder() {
        return new Builder<>();
    }

    @Override
    public AttributeChangesCheckerResult getResult(T oldObj, T newObj) {
        if (oldObj == newObj) {
            return new AttributeChangesCheckerResult(new HashMap<>(), true);
        }

        Map<String, Difference> diffMap = new HashMap<>();

        Object firstNonNull = Stream.of(newObj, oldObj).filter(Objects::nonNull).findFirst().get();
        Class<?> sourceClass = firstNonNull.getClass();

        for (AttributeMetaInfo attributeToCheck : attributesToCheck) {
            Object newAttrVal = attributeToCheck.fieldPath().getValueFromObject(newObj);
            Object oldAttrVal = attributeToCheck.fieldPath().getValueFromObject(oldObj);

            checkWithInnerCheckerAndPutInMapIfHasDiff(attributeToCheck, newAttrVal, oldAttrVal, attributeToCheck.changesChecker(), diffMap, sourceClass);

            if (!diffMap.isEmpty() && stopOnFirstDiff) {
                break;
            }
        }

        return new AttributeChangesCheckerResult(diffMap, diffMap.isEmpty());
    }

    private void checkWithInnerCheckerAndPutInMapIfHasDiff(AttributeMetaInfo attributeToCheck, Object newAttrVal, Object oldAttrVal, ChangesChecker changesChecker, Map<String, Difference> diffMap, Class<?> sourceClass) {
        if (newAttrVal == oldAttrVal) {
            return;
        }

        if (newAttrVal == null || oldAttrVal == null) {
            diffMap.put(attributeToCheck.fieldPath().path(), new AttributeDifference(attributeToCheck.fieldPath().path(), sourceClass, attributeToCheck.fieldPath().clazz(), oldAttrVal, newAttrVal));
            return;
        }

        if (changesChecker != null) {
            if (CollectionChangesChecker.class.isAssignableFrom(changesChecker.getClass())) {
                if (!Collection.class.isAssignableFrom(newAttrVal.getClass()) || !Collection.class.isAssignableFrom(oldAttrVal.getClass())) {
                    throw new RuntimeException("One of objects is not a collection");
                }

                CollectionChangesChecker collectionChangesChecker = (CollectionChangesChecker) changesChecker;
                CollectionChangesCheckerResult collectionChangesCheckerResult = collectionChangesChecker.getResult((Collection) oldAttrVal, (Collection) newAttrVal);
                diffMap.put(attributeToCheck.fieldPath().path(), collectionChangesCheckerResult);
                return;
            }

            if (AttributeChangesChecker.class.isAssignableFrom(changesChecker.getClass())) {
                AttributeChangesChecker attributeChangesChecker = (AttributeChangesChecker) changesChecker;
                AttributeChangesCheckerResult attributeChangesCheckerResult = attributeChangesChecker.getResult(oldAttrVal, newAttrVal);
                diffMap.put(attributeToCheck.fieldPath().path(), attributeChangesCheckerResult);
                return;
            }
        }


        if (attributeToCheck.equalsMethodReflectionRef() != null) {
            Boolean result = (Boolean) ReflectionUtils.invokeMethod(attributeToCheck.equalsMethodReflectionRef(), oldAttrVal, newAttrVal);

            if (result != null && result) {
                diffMap.put(attributeToCheck.fieldPath().path(), new AttributeDifference(attributeToCheck.fieldPath().path(), sourceClass, attributeToCheck.fieldPath().clazz(), oldAttrVal, newAttrVal));
            }

            return;
        }

        if (!CollectionUtils.isEmpty(attributeToCheck.equalsFields())) {
            for (FieldPath equalsField : attributeToCheck.equalsFields()) {
                Class<?> attributeClass = equalsField.getLast().clazz();
                Object oldObjVal = equalsField.getValueFromObject(oldAttrVal);
                Object newObjVal = equalsField.getValueFromObject(newAttrVal);

                if (!Objects.equals(oldObjVal, newObjVal)) {
                    diffMap.put(attributeToCheck.fieldPath().path(), new AttributeDifference(equalsField.path(), sourceClass, attributeClass, oldObjVal, newObjVal));
                }
            }

            return;
        }

        if (attributeToCheck.biEqualsMethodCodeRef() != null) {
            boolean biEqualsResult = attributeToCheck.biEqualsMethodCodeRef().test(oldAttrVal, newAttrVal);
            if (!biEqualsResult) {
                diffMap.put(attributeToCheck.fieldPath().path(), new AttributeDifference(attributeToCheck.fieldPath().path(), sourceClass, attributeToCheck.fieldPath().clazz(), oldAttrVal, newAttrVal));
            }
        }

        boolean equalsResult = Objects.equals(newAttrVal, oldAttrVal);
        if (!equalsResult) {
            diffMap.put(attributeToCheck.fieldPath().path(), new AttributeDifference(attributeToCheck.fieldPath().path(), sourceClass, attributeToCheck.fieldPath().clazz(), oldAttrVal, newAttrVal));
        }
    }

    public static class Builder<T> {

        Set<AttributeMetaInfo> attributesToCheck;

        boolean stopOnFirstDiff;

        public Builder<T> addAttributeToCheck(AttributeMetaInfo attribute) {
            if (attributesToCheck == null) {
                attributesToCheck = new OrderedHashSet<>();
            }

            attributesToCheck.add(attribute);

            return this;
        }

        public Builder<T> stopOnFirstDiff() {
            this.stopOnFirstDiff = true;

            return this;
        }

        public DefaultAttributeChangesChecker<T> build() {
            return new DefaultAttributeChangesChecker<>(attributesToCheck, stopOnFirstDiff);
        }
    }
}
