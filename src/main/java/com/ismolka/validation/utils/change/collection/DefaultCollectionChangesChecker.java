package com.ismolka.validation.utils.change.collection;

import com.ismolka.validation.constraints.constant.CollectionOperation;
import com.ismolka.validation.utils.change.attribute.AttributeChangesCheckerResult;
import com.ismolka.validation.utils.change.attribute.AttributeMetaInfo;
import com.ismolka.validation.utils.change.attribute.DefaultAttributeChangesChecker;
import com.ismolka.validation.validator.metainfo.FieldPath;
import com.ismolka.validation.validator.utils.MetaInfoExtractorUtil;
import org.antlr.v4.runtime.misc.OrderedHashSet;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.*;
import java.util.function.BiPredicate;
import java.util.stream.Stream;

public class DefaultCollectionChangesChecker<T> extends DefaultAttributeChangesChecker<T> implements CollectionChangesChecker<T> {

    private Set<CollectionOperation> forOperations;

    private Method mainEqualsMethodReflectionRef;

    private BiPredicate mainBiEqualsMethodCodeRef;

    private Set<FieldPath> fieldsForMatching;

    private Set<FieldPath> mainEqualsFields;


    public DefaultCollectionChangesChecker() {
        super();
        this.forOperations = new HashSet<>();
        this.fieldsForMatching = new OrderedHashSet<>();
        this.mainEqualsFields = new OrderedHashSet<>();
    }

    public DefaultCollectionChangesChecker(Set<AttributeMetaInfo> attributesToCheck, boolean stopOnFirstDiff, Set<CollectionOperation> forOperations, Method mainEqualsMethodReflectionRef, BiPredicate mainBiEqualsMethodCodeRef, Set<FieldPath> fieldsForMatching, Set<FieldPath> mainEqualsFields) {
        super(attributesToCheck, stopOnFirstDiff);
        this.forOperations = forOperations;
        this.mainEqualsMethodReflectionRef = mainEqualsMethodReflectionRef;
        this.mainBiEqualsMethodCodeRef = mainBiEqualsMethodCodeRef;
        this.fieldsForMatching = fieldsForMatching;
        this.mainEqualsFields = mainEqualsFields;
    }

    public static Builder builder(Class<?> collectionClass) {
        return new Builder(collectionClass);
    }

    @Override
    public CollectionChangesCheckerResult getResult(Collection<T> oldCollection, Collection<T> newCollection) {
        Map<CollectionOperation, Set<CollectionElementDifference>> collectionDifference = new HashMap<>();

        boolean needsStop = false;

        if (!fieldsForMatching.isEmpty()) {
            Map<String, MatchingElement> collectionByKeyMap = new HashMap<>();
            int newObjIndex = 0;
            for (Object newObject : newCollection) {
                collectionByKeyMap.put(getKeyString(newObject), new MatchingElement(newObject, false, newObjIndex));
                newObjIndex++;
            }

            int actualObjIndex = 0;

            for (Object actualObj : oldCollection) {
                String objectKeyString = getKeyString(actualObj);

                MatchingElement matched = collectionByKeyMap.get(objectKeyString);
                if (matched == null) {
                    if (forOperations.contains(CollectionOperation.REMOVE)) {
                        if (!collectionDifference.containsKey(CollectionOperation.REMOVE)) {
                            collectionDifference.put(CollectionOperation.REMOVE, new OrderedHashSet<>());
                        }

                        CollectionElementDifference collectionElementDifference = new CollectionElementDifference(null, actualObj, null, actualObjIndex, null);

                        collectionDifference.get(CollectionOperation.REMOVE).add(collectionElementDifference);
                        if (stopOnFirstDiff) {
                            needsStop = true;
                            break;
                        }
                    }
                } else {
                    matched.setMatchWasFound(true);

                    if (forOperations.contains(CollectionOperation.UPDATE)) {

                        CollectionElementDifference collectionElementDifference = checkAndReturnDiff(actualObj, matched.elementValue, actualObjIndex, matched.elementIndex);
                        if (collectionElementDifference != null) {
                            if (!collectionDifference.containsKey(CollectionOperation.UPDATE)) {
                                collectionDifference.put(CollectionOperation.UPDATE, new OrderedHashSet<>());
                            }

                            collectionDifference.get(CollectionOperation.UPDATE).add(collectionElementDifference);
                            if (stopOnFirstDiff) {
                                needsStop = true;
                                break;
                            }
                        }
                    }
                }

                actualObjIndex++;
            }

            if (forOperations.contains(CollectionOperation.ADD)) {
                if (!needsStop) {
                    for (MatchingElement matchingElement : collectionByKeyMap.values()) {
                        if (!matchingElement.matchWasFound) {
                            if (!collectionDifference.containsKey(CollectionOperation.ADD)) {
                                collectionDifference.put(CollectionOperation.ADD, new OrderedHashSet<>());
                            }

                            CollectionElementDifference collectionElementDifference = new CollectionElementDifference(null, null, matchingElement.elementValue, null, matchingElement.elementIndex);
                            collectionDifference.get(CollectionOperation.ADD).add(collectionElementDifference);
                            if (stopOnFirstDiff) {
                                needsStop = true;
                                break;
                            }
                        }
                    }
                }
            }
        } else {
            List<Object> list = new ArrayList<>(newCollection);
            List<Object> actualList = new ArrayList<>(oldCollection);

            if (forOperations.contains(CollectionOperation.UPDATE)) {
                for (int i = 0; i < list.size(); i++) {
                    if (i >= actualList.size()) {
                        break;
                    }

                    Object obj = list.get(i);
                    Object actualObj = actualList.get(i);

                    CollectionElementDifference collectionElementDifference = checkAndReturnDiff(actualObj, obj, i, i);
                    if (collectionElementDifference != null) {
                        if (!collectionDifference.containsKey(CollectionOperation.UPDATE)) {
                            collectionDifference.put(CollectionOperation.UPDATE, new OrderedHashSet<>());
                        }

                        collectionDifference.get(CollectionOperation.UPDATE).add(collectionElementDifference);
                        if (stopOnFirstDiff) {
                            needsStop = true;
                            break;
                        }
                    }
                }
            }

            if (forOperations.contains(CollectionOperation.ADD)) {
                if (!(needsStop) && list.size() > actualList.size()) {
                    for (int i = actualList.size(); i < list.size(); i++) {
                        if (!collectionDifference.containsKey(CollectionOperation.ADD)) {
                            collectionDifference.put(CollectionOperation.ADD, new OrderedHashSet<>());
                        }

                        CollectionElementDifference collectionElementDifference = new CollectionElementDifference(null, null, list.get(i), null, i);
                        collectionDifference.get(CollectionOperation.ADD).add(collectionElementDifference);

                        if (stopOnFirstDiff) {
                            needsStop = true;
                        }
                    }
                }
            }


            if (forOperations.contains(CollectionOperation.REMOVE)) {
                if (!(needsStop) && actualList.size() > list.size()) {
                    for (int i = list.size(); i < actualList.size(); i++) {
                        if (!collectionDifference.containsKey(CollectionOperation.REMOVE)) {
                            collectionDifference.put(CollectionOperation.REMOVE, new OrderedHashSet<>());
                        }
                        CollectionElementDifference collectionElementDifference = new CollectionElementDifference(null, actualList.get(i), null, i, null);
                        collectionDifference.get(CollectionOperation.REMOVE).add(collectionElementDifference);

                        if (stopOnFirstDiff) {
                            break;
                        }
                    }
                }
            }
        }

        return new CollectionChangesCheckerResult(collectionDifference, collectionDifference.isEmpty());
    }

    private CollectionElementDifference checkAndReturnDiff(Object oldElement, Object newElement, Integer oldObjectIndex, Integer newObjectIndex) {
        if (oldElement == newElement) {
            return null;
        }

        if (mainEqualsMethodReflectionRef != null) {
            Object firstNonNull = Stream.of(newElement, oldElement).filter(Objects::nonNull).findFirst().get();
            Object argObj = Stream.of(newElement, oldElement).filter(obj -> obj != firstNonNull).findFirst().get();

            Boolean result = (Boolean) ReflectionUtils.invokeMethod(mainEqualsMethodReflectionRef, firstNonNull, argObj);
            if (result != null && result) {
                return new CollectionElementDifference(null, oldElement, newElement, oldObjectIndex, newObjectIndex);
            }
        }

        if (!CollectionUtils.isEmpty(mainEqualsFields)) {
            //todo заполнять???
            for (FieldPath equalsField : mainEqualsFields) {
                Object oldObjVal = equalsField.getValueFromObject(oldElement);
                Object newObjVal = equalsField.getValueFromObject(newElement);

                if (!Objects.equals(oldObjVal, newObjVal)) {
                    return new CollectionElementDifference(null, oldElement, newElement, oldObjectIndex, newObjectIndex);
                }
            }
        }

        if (mainBiEqualsMethodCodeRef != null) {
            boolean isEquals = mainBiEqualsMethodCodeRef.test(oldElement, newElement);

            if (!isEquals) {
                return new CollectionElementDifference(null, oldElement, newElement, oldObjectIndex, newObjectIndex);
            }
        }

        if (!CollectionUtils.isEmpty(attributesToCheck)) {
            AttributeChangesCheckerResult resultFromAttributesCheck = getResult((T) oldElement, (T) newElement);
            if (!resultFromAttributesCheck.equalsResult()) {
                return new CollectionElementDifference(resultFromAttributesCheck.differenceMap(), oldElement, newElement, oldObjectIndex, newObjectIndex);
            }
        }

        return null;
    }

    private String getKeyString(Object object) {
        if (object == null) {
            return "null";
        }

        StringBuilder keyString = new StringBuilder();
        for (FieldPath fieldPath : fieldsForMatching) {
            Object fieldVal = fieldPath.getValueFromObject(object);
            keyString.append(";").append(fieldVal);
        }

        return keyString.toString();
    }


    private static class MatchingElement {
        private final Object elementValue;

        private boolean matchWasFound;

        private final Integer elementIndex;


        public MatchingElement(Object elementValue, boolean matchWasFound, Integer elementIndex) {
            this.elementValue = elementValue;
            this.matchWasFound = matchWasFound;
            this.elementIndex = elementIndex;
        }

        public void setMatchWasFound(boolean matchWasFound) {
            this.matchWasFound = matchWasFound;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            MatchingElement that = (MatchingElement) o;
            return matchWasFound == that.matchWasFound && Objects.equals(elementValue, that.elementValue) && Objects.equals(elementIndex, that.elementIndex);
        }

        @Override
        public int hashCode() {
            return Objects.hash(elementValue, matchWasFound, elementIndex);
        }
    }

    public static class Builder {

        Class<?> collectionGenericClass;

        Set<AttributeMetaInfo> attributesToCheck;
        boolean stopOnFirstDiff;

        Set<CollectionOperation> forOperations;

        Method mainEqualsMethodReflectionRef;

        BiPredicate mainBiEqualsMethodCodeRef;

        Set<FieldPath> fieldsForMatching;

        Set<FieldPath> mainEqualsFields;


        public Builder(Class<?> collectionGenericClass) {
            this.collectionGenericClass = collectionGenericClass;
        }

        public Builder addAttributeToCheck(AttributeMetaInfo attribute) {
            if (attributesToCheck == null) {
                attributesToCheck = new OrderedHashSet<>();
            }

            attributesToCheck.add(attribute);

            return this;
        }

        public Builder stopOnFirstDiff() {
            this.stopOnFirstDiff = true;

            return this;
        }

        public Builder forOperation(CollectionOperation operation) {
            if (forOperations == null) {
                forOperations = new HashSet<>();
            }

            forOperations.add(operation);

            return this;
        }

        public Builder forOperations(CollectionOperation... operations) {
            if (forOperations == null) {
                forOperations = new HashSet<>();
            }

            forOperations.addAll(Arrays.stream(operations).toList());

            return this;
        }

        public Builder mainMethodEqualsReflection(Method mainEqualsMethodReflectionRef) {
            this.mainEqualsMethodReflectionRef = mainEqualsMethodReflectionRef;

            return this;
        }


        public Builder mainBiEqualsMethod(BiPredicate mainBiEqualsMethodCodeRef) {
            this.mainBiEqualsMethodCodeRef = mainBiEqualsMethodCodeRef;

            return this;
        }

        public Builder addFieldForMatching(String fieldPathForMatching) {
            if (fieldsForMatching == null) {
                fieldsForMatching = new OrderedHashSet<>();
            }

            FieldPath fieldForMatching = MetaInfoExtractorUtil.extractFieldPathMetaInfo(fieldPathForMatching, collectionGenericClass);

            fieldsForMatching.add(fieldForMatching);

            return this;
        }

        public Builder addMainEqualsField(String mainEqualsFieldPath) {
            if (mainEqualsFields == null) {
                mainEqualsFields = new OrderedHashSet<>();
            }

            FieldPath fieldForMainEquals = MetaInfoExtractorUtil.extractFieldPathMetaInfo(mainEqualsFieldPath, collectionGenericClass);

            fieldsForMatching.add(fieldForMainEquals);

            return this;
        }
    }
}
