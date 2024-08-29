package com.ismolka.validation.utils.change.collection;

import com.ismolka.validation.constraints.constant.CollectionOperation;
import com.ismolka.validation.utils.change.Difference;
import com.ismolka.validation.utils.change.attribute.AttributeChangesCheckerResult;
import com.ismolka.validation.utils.change.attribute.AttributeDifference;
import com.ismolka.validation.utils.change.attribute.AttributeMetaInfo;
import com.ismolka.validation.utils.change.attribute.DefaultAttributeChangesChecker;
import com.ismolka.validation.validator.metainfo.FieldPath;
import org.antlr.v4.runtime.misc.OrderedHashSet;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.*;
import java.util.function.BiPredicate;
import java.util.stream.Stream;

public class DefaultCollectionChangesChecker<T> extends DefaultAttributeChangesChecker<T> implements CollectionChangesChecker<T> {

    private final Class<T> collectionGenericClass;

    private final Set<CollectionOperation> forOperations;

    private Method mainEqualsMethodReflectionRef;

    private BiPredicate<T, T> mainBiEqualsMethodCodeRef;

    private final Set<FieldPath> fieldsForMatching;

    private final Set<FieldPath> mainEqualsFields;


    public DefaultCollectionChangesChecker(Class<T> collectionGenericClass) {
        super();
        this.collectionGenericClass = collectionGenericClass;
        this.forOperations = new HashSet<>();
        this.fieldsForMatching = new OrderedHashSet<>();
        this.mainEqualsFields = new OrderedHashSet<>();
    }

    public DefaultCollectionChangesChecker(Class<T> collectionGenericClass, Set<AttributeMetaInfo> attributesToCheck, boolean stopOnFirstDiff, Set<CollectionOperation> forOperations, Method mainEqualsMethodReflectionRef, BiPredicate<T, T> mainBiEqualsMethodCodeRef, Set<FieldPath> fieldsForMatching, Set<FieldPath> mainEqualsFields) {
        super(attributesToCheck, stopOnFirstDiff);
        this.collectionGenericClass = collectionGenericClass;
        this.forOperations = forOperations;
        this.mainEqualsMethodReflectionRef = mainEqualsMethodReflectionRef;
        this.mainBiEqualsMethodCodeRef = mainBiEqualsMethodCodeRef;
        this.fieldsForMatching = fieldsForMatching;
        this.mainEqualsFields = mainEqualsFields;
    }

    @Override
    public CollectionChangesCheckerResult<T> getResult(Collection<T> oldCollection, Collection<T> newCollection) {
        Map<CollectionOperation, Set<CollectionElementDifference<T>>> collectionDifference = new HashMap<>();

        boolean needsStop = false;

        if (!CollectionUtils.isEmpty(fieldsForMatching)) {
            Map<String, MatchingElement> collectionByKeyMap = new HashMap<>();
            int newObjIndex = 0;
            for (Object newObject : newCollection) {
                collectionByKeyMap.put(getKeyString(newObject), new MatchingElement(newObject, false, newObjIndex));
                newObjIndex++;
            }

            int actualObjIndex = 0;

            for (T actualObj : oldCollection) {
                String objectKeyString = getKeyString(actualObj);

                MatchingElement matched = collectionByKeyMap.get(objectKeyString);
                if (matched == null) {
                    if (forOperations.contains(CollectionOperation.REMOVE)) {
                        if (!collectionDifference.containsKey(CollectionOperation.REMOVE)) {
                            collectionDifference.put(CollectionOperation.REMOVE, new OrderedHashSet<>());
                        }

                        CollectionElementDifference<T> collectionElementDifference = new CollectionElementDifference<>(null, actualObj, null, actualObjIndex, null);

                        collectionDifference.get(CollectionOperation.REMOVE).add(collectionElementDifference);
                        if (stopOnFirstDiff) {
                            needsStop = true;
                            break;
                        }
                    }
                } else {
                    matched.setMatchWasFound(true);

                    if (forOperations.contains(CollectionOperation.UPDATE)) {

                        CollectionElementDifference<T> collectionElementDifference = checkAndReturnDiff((T) actualObj, (T) matched.elementValue, actualObjIndex, matched.elementIndex);
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

                            CollectionElementDifference<T> collectionElementDifference = new CollectionElementDifference<>(null, null, (T) matchingElement.elementValue, null, matchingElement.elementIndex);
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
            List<T> list = new ArrayList<>(newCollection);
            List<T> actualList = new ArrayList<>(oldCollection);

            if (forOperations.contains(CollectionOperation.UPDATE)) {
                for (int i = 0; i < list.size(); i++) {
                    if (i >= actualList.size()) {
                        break;
                    }

                    T obj = list.get(i);
                    T actualObj = actualList.get(i);

                    CollectionElementDifference<T> collectionElementDifference = checkAndReturnDiff(actualObj,  obj, i, i);
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

                        CollectionElementDifference<T> collectionElementDifference = new CollectionElementDifference<T>(null, null, list.get(i), null, i);
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
                        CollectionElementDifference<T> collectionElementDifference = new CollectionElementDifference<T>(null, actualList.get(i), null, i, null);
                        collectionDifference.get(CollectionOperation.REMOVE).add(collectionElementDifference);

                        if (stopOnFirstDiff) {
                            break;
                        }
                    }
                }
            }
        }

        return new CollectionChangesCheckerResult<>(collectionDifference, collectionDifference.isEmpty());
    }

    private CollectionElementDifference<T> checkAndReturnDiff(T oldElement, T newElement, Integer oldObjectIndex, Integer newObjectIndex) {
        if (oldElement == newElement) {
            return null;
        }

        if (mainEqualsMethodReflectionRef != null) {
            Object firstNonNull = Stream.of(newElement, oldElement).filter(Objects::nonNull).findFirst().get();
            Object argObj = Stream.of(newElement, oldElement).filter(obj -> obj != firstNonNull).findFirst().get();

            Boolean result = (Boolean) ReflectionUtils.invokeMethod(mainEqualsMethodReflectionRef, firstNonNull, argObj);
            if (result != null && result) {
                return new CollectionElementDifference<>(null, oldElement, newElement, oldObjectIndex, newObjectIndex);
            }
        }

        if (!CollectionUtils.isEmpty(mainEqualsFields)) {
            //todo заполнять???
            for (FieldPath equalsField : mainEqualsFields) {
                Class<?> attributeClass = equalsField.getLast().clazz();
                Class<?> attributeDeclaringClass = equalsField.getLast().declaringClass();
                Object oldObjVal = equalsField.getValueFromObject(oldElement);
                Object newObjVal = equalsField.getValueFromObject(newElement);

                if (!Objects.equals(oldObjVal, newObjVal)) {
                    Map<String, Difference> diffMap = new HashMap<>();
                    diffMap.put(equalsField.path(), new AttributeDifference(equalsField.path(), collectionGenericClass, attributeDeclaringClass, attributeClass, oldObjVal, newObjVal));
                    return new CollectionElementDifference<>(diffMap, oldElement, newElement, oldObjectIndex, newObjectIndex);
                }
            }
        }

        if (mainBiEqualsMethodCodeRef != null) {
            boolean isEquals = mainBiEqualsMethodCodeRef.test(oldElement, newElement);

            if (!isEquals) {
                return new CollectionElementDifference<>(null, oldElement, newElement, oldObjectIndex, newObjectIndex);
            }
        }

        if (!CollectionUtils.isEmpty(attributesToCheck)) {
            AttributeChangesCheckerResult resultFromAttributesCheck = getResult((T) oldElement, (T) newElement);
            if (!resultFromAttributesCheck.equalsResult()) {
                return new CollectionElementDifference<>(resultFromAttributesCheck.differenceMap(), oldElement, newElement, oldObjectIndex, newObjectIndex);
            }
        }

        boolean equalsResult = Objects.equals(oldElement, newElement);
        if (!equalsResult) {
            return new CollectionElementDifference<>(null, oldElement, newElement, oldObjectIndex, newObjectIndex);
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
}
