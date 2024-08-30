package com.ismolka.validation.utils.change.collection;

import com.ismolka.validation.utils.constant.CollectionOperation;
import com.ismolka.validation.utils.change.value.ValueChangesCheckerResult;
import com.ismolka.validation.utils.change.value.ValueCheckDescriptor;
import com.ismolka.validation.utils.change.value.DefaultValueChangesChecker;
import com.ismolka.validation.utils.metainfo.FieldPath;
import org.antlr.v4.runtime.misc.OrderedHashSet;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Method;
import java.util.*;
import java.util.function.BiPredicate;

public class DefaultCollectionChangesChecker<T> extends DefaultValueChangesChecker<T> implements CollectionChangesChecker<T> {

    private final Class<T> collectionGenericClass;

    private final Set<CollectionOperation> forOperations;

    private final Set<FieldPath> fieldsForMatching;


    protected DefaultCollectionChangesChecker(Class<T> collectionGenericClass, Set<ValueCheckDescriptor<?>> attributesCheckDescriptors, boolean stopOnFirstDiff, Method globalEqualsMethodReflectionRef, BiPredicate<T, T> globalBiEqualsMethodCodeRef, Set<FieldPath> globalEqualsFields, Set<CollectionOperation> forOperations, Set<FieldPath> fieldsForMatching) {
        super(attributesCheckDescriptors, stopOnFirstDiff, globalEqualsMethodReflectionRef, globalBiEqualsMethodCodeRef, globalEqualsFields);
        this.collectionGenericClass = collectionGenericClass;
        this.forOperations = forOperations;
        this.fieldsForMatching = fieldsForMatching;
    }

    @Override
    public CollectionChangesCheckerResult<T> getResult(Collection<T> oldCollection, Collection<T> newCollection) {
        if (oldCollection == newCollection) {
            return new CollectionChangesCheckerResult<>(collectionGenericClass, null, true);
        }

        if (oldCollection == null || newCollection == null) {
            return returnResultWhenOneIsNull(oldCollection, newCollection);
        }

        Map<CollectionOperation, Set<CollectionElementDifference<T>>> collectionDifference = new HashMap<>();

        boolean needsStop = false;

        if (!CollectionUtils.isEmpty(fieldsForMatching)) {
            Map<String, MatchingElement<T>> collectionByKeyMap = new HashMap<>();

            Set<MatchingElement<T>> objectsWithNullFieldsMatchingInNewCollection = new OrderedHashSet<>();
            Set<MatchingElement<T>> objectsWithNullFieldsMatchingInOldCollection = new OrderedHashSet<>();

            int newObjIndex = 0;
            for (T newObject : newCollection) {
                if (fieldsForMatchingIsNullForObject(newObject)) {
                    objectsWithNullFieldsMatchingInNewCollection.add(new MatchingElement<>(newObject, false, newObjIndex));
                    continue;
                }

                collectionByKeyMap.put(getKeyString(newObject), new MatchingElement<>(newObject, false, newObjIndex));
                newObjIndex++;
            }

            int actualObjIndex = 0;

            for (T actualObj : oldCollection) {
                if (fieldsForMatchingIsNullForObject(actualObj)) {
                    objectsWithNullFieldsMatchingInOldCollection.add(new MatchingElement<>(actualObj, false, actualObjIndex));
                    continue;
                }

                String objectKeyString = getKeyString(actualObj);

                MatchingElement<T> matched = collectionByKeyMap.get(objectKeyString);
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
                        CollectionElementDifference<T> collectionElementDifference = checkAndReturnDiff(actualObj, matched.elementValue, actualObjIndex, matched.elementIndex);
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

            if (!objectsWithNullFieldsMatchingInNewCollection.isEmpty() && forOperations.contains(CollectionOperation.ADD) && !needsStop) {
                if (!collectionDifference.containsKey(CollectionOperation.ADD)) {
                    collectionDifference.put(CollectionOperation.ADD, new OrderedHashSet<>());
                }

                for (MatchingElement<T> objectWithNullFieldsMatchingInNewCollection : objectsWithNullFieldsMatchingInNewCollection) {
                    CollectionElementDifference<T> collectionElementDifference = new CollectionElementDifference<>(null, null, objectWithNullFieldsMatchingInNewCollection.elementValue, null, objectWithNullFieldsMatchingInNewCollection.elementIndex);
                    collectionDifference.get(CollectionOperation.ADD).add(collectionElementDifference);
                    if (stopOnFirstDiff) {
                        needsStop = true;
                        break;
                    }
                }
            }

            if (!objectsWithNullFieldsMatchingInOldCollection.isEmpty() && forOperations.contains(CollectionOperation.REMOVE) && !needsStop) {
                if (!collectionDifference.containsKey(CollectionOperation.REMOVE)) {
                    collectionDifference.put(CollectionOperation.REMOVE, new OrderedHashSet<>());
                }

                for (MatchingElement<T> objectWithNullFieldsMatchingInOldCollection : objectsWithNullFieldsMatchingInOldCollection) {
                    CollectionElementDifference<T> collectionElementDifference = new CollectionElementDifference<>(null, objectWithNullFieldsMatchingInOldCollection.elementValue, null, objectWithNullFieldsMatchingInOldCollection.elementIndex, null);
                    collectionDifference.get(CollectionOperation.REMOVE).add(collectionElementDifference);
                    if (stopOnFirstDiff) {
                        needsStop = true;
                        break;
                    }
                }
            }

            if (forOperations.contains(CollectionOperation.ADD) && !needsStop) {
                for (MatchingElement<T> matchingElement : collectionByKeyMap.values()) {
                    if (!matchingElement.matchWasFound) {
                        if (!collectionDifference.containsKey(CollectionOperation.ADD)) {
                            collectionDifference.put(CollectionOperation.ADD, new OrderedHashSet<>());
                        }

                        CollectionElementDifference<T> collectionElementDifference = new CollectionElementDifference<>(null, null, matchingElement.elementValue, null, matchingElement.elementIndex);
                        collectionDifference.get(CollectionOperation.ADD).add(collectionElementDifference);
                        if (stopOnFirstDiff) {
                            break;
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

                        CollectionElementDifference<T> collectionElementDifference = new CollectionElementDifference<>(null, null, list.get(i), null, i);
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
                        CollectionElementDifference<T> collectionElementDifference = new CollectionElementDifference<>(null, actualList.get(i), null, i, null);
                        collectionDifference.get(CollectionOperation.REMOVE).add(collectionElementDifference);

                        if (stopOnFirstDiff) {
                            break;
                        }
                    }
                }
            }
        }

        return new CollectionChangesCheckerResult<>(collectionGenericClass, collectionDifference, collectionDifference.isEmpty());
    }

    private CollectionChangesCheckerResult<T> returnResultWhenOneIsNull(Collection<T> oldCollection, Collection<T> newCollection) {
        Set<CollectionElementDifference<T>> collectionDifference = new OrderedHashSet<>();

        if (oldCollection == null) {
            int index = 0;
            for (T newObject : newCollection) {
                collectionDifference.add(new CollectionElementDifference<>(null, null, newObject, null, index));
                index++;
            }
        }

        if (newCollection == null) {
            int index = 0;
            for (T oldObject : oldCollection) {
                collectionDifference.add(new CollectionElementDifference<>(null, oldObject, null, index, null));
                index++;
            }
        }

        CollectionOperation operation = oldCollection == null ? CollectionOperation.ADD : CollectionOperation.REMOVE;

        return new CollectionChangesCheckerResult<>(collectionGenericClass, Map.of(operation, collectionDifference), false);
    }

    private CollectionElementDifference<T> checkAndReturnDiff(T oldElement, T newElement, Integer oldObjectIndex, Integer newObjectIndex) {
        if (oldElement == newElement) {
            return null;
        }

        ValueChangesCheckerResult valueChangesCheckerResult = getResult(oldElement, newElement);
        if (!valueChangesCheckerResult.equalsResult()) {
            return new CollectionElementDifference<>(valueChangesCheckerResult.differenceMap(), oldElement, newElement, oldObjectIndex, newObjectIndex);
        }

        return null;
    }


    private boolean fieldsForMatchingIsNullForObject(Object object) {
        if (object == null) {
            return true;
        }

        boolean isNull = true;

        for (FieldPath fieldPath : fieldsForMatching) {
            Object fieldVal = fieldPath.getValueFromObject(object);
            if (fieldVal != null) {
                isNull = false;
                break;
            }
        }

        return isNull;
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


    private static class MatchingElement<T> {
        private final T elementValue;

        private boolean matchWasFound;

        private final Integer elementIndex;


        public MatchingElement(T elementValue, boolean matchWasFound, Integer elementIndex) {
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
            MatchingElement<?> that = (MatchingElement<?>) o;
            return matchWasFound == that.matchWasFound && Objects.equals(elementValue, that.elementValue) && Objects.equals(elementIndex, that.elementIndex);
        }

        @Override
        public int hashCode() {
            return Objects.hash(elementValue, matchWasFound, elementIndex);
        }
    }
}
