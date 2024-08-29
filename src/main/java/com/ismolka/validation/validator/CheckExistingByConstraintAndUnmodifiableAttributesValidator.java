package com.ismolka.validation.validator;

import com.ismolka.validation.constraints.CheckExistingByConstraintAndUnmodifiableAttributes;
import com.ismolka.validation.utils.change.constant.CollectionOperation;
import com.ismolka.validation.constraints.inner.UnmodifiableAttribute;
import com.ismolka.validation.constraints.inner.UnmodifiableCollection;
import com.ismolka.validation.validator.metainfo.FieldPath;
import com.ismolka.validation.validator.utils.HibernateConstraintValidationUtils;
import com.ismolka.validation.validator.utils.MetaInfoExtractorUtil;
import jakarta.persistence.EntityGraph;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.antlr.v4.runtime.misc.OrderedHashSet;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class CheckExistingByConstraintAndUnmodifiableAttributesValidator extends AbstractEntityManagerConstraintValidator<CheckExistingByConstraintAndUnmodifiableAttributes, Object> implements ConstraintValidator<CheckExistingByConstraintAndUnmodifiableAttributes, Object> {

    private static final Map<Class<?>, CheckExistingMetaInfo> META_INFO = new ConcurrentHashMap<>();

    private static final Integer MAX_RESULT = 1;

    private static final String DOESNT_EXIST_FIELDS_PARAM_NAME = "doesntExistFields";

    private static final String DOESNT_EXIST_FIELD_VALUES_PARAM_NAME = "doesntExistFieldValues";


    private static final String FIELD_DIFF_NAME_PARAM_NAME = "fieldDiffName";

    private static final String FIELD_DIFF_VALUE_NEW_PARAM_NAME = "fieldDiffValueNew";

    private static final String FIELD_DIFF_VALUE_OLD_PARAM_NAME = "fieldDiffValueOld";

    private String[] constraintKeyFields;

    private UnmodifiableAttribute[] unmodifiableAttributes;

    private UnmodifiableCollection[] unmodifiableCollections;

    private boolean loadByConstraint;

    private boolean stopUnmodifiableCheckOnFirstMismatch;

    private String loadingByUsingNamedEntityGraph;

    @Override
    public void initialize(CheckExistingByConstraintAndUnmodifiableAttributes constraintAnnotation) {
        this.constraintKeyFields = constraintAnnotation.constraintKey().value();
        this.loadByConstraint = constraintAnnotation.loadByConstraint();
        this.loadingByUsingNamedEntityGraph = constraintAnnotation.loadingByUsingNamedEntityGraph();
        this.unmodifiableAttributes = constraintAnnotation.unmodifiableAttributes();
        this.unmodifiableCollections = constraintAnnotation.unmodifiableCollections();
        this.stopUnmodifiableCheckOnFirstMismatch = constraintAnnotation.stopUnmodifiableCheckOnFirstMismatch();
        this.message = constraintAnnotation.message();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        Class<?> clazz = value.getClass();

        if (!META_INFO.containsKey(clazz)) {
            extractAndCashMetaDataForClass(clazz);
        }

        CheckExistingMetaInfo checkExistingMetaInfo = META_INFO.get(clazz);

        Set<FieldPath> constraintKey = checkExistingMetaInfo.existingConstraint();

        CriteriaQuery<Object> criteriaQuery = createCriteriaQuery(clazz, constraintKey, value);

        TypedQuery<Object> query = em.createQuery(criteriaQuery);
        query.setMaxResults(MAX_RESULT);

        if (loadByConstraint && !loadingByUsingNamedEntityGraph.isEmpty()) {
            EntityGraph<?> entityGraph = em.getEntityGraph(loadingByUsingNamedEntityGraph);
            query.setHint("jakarta.persistence.fetchgraph", entityGraph);
        }

        List<Object> resultList = query.getResultList();
        boolean isEmpty = resultList.isEmpty();

        if (isEmpty) {
            fillContextValidatorForExistingConstraint(context, constraintKey, value);
            return false;
        }

        Object existedVal = resultList.get(0);

        boolean mismatchesFound = false;

        List<CheckUnmodifiableResult> checkUnmodifiableResults = checkUnmodifiableAttributes(value, existedVal, checkExistingMetaInfo);
        if (!checkUnmodifiableResults.isEmpty()) {
            mismatchesFound = true;
            fillContextValidatorForCheckUnmodifiableResult(context, checkUnmodifiableResults);

            if (stopUnmodifiableCheckOnFirstMismatch) {
                return false;
            }
        }

        List<CheckUnmodifiableResult> checkUnmodifiableResultsForCollections = checkUnmodifiableCollections(value, existedVal, checkExistingMetaInfo);
        if (!checkUnmodifiableResultsForCollections.isEmpty()) {
            mismatchesFound = true;
            fillContextValidatorForCheckUnmodifiableResult(context, checkUnmodifiableResultsForCollections);

            if (stopUnmodifiableCheckOnFirstMismatch) {
                return false;
            }
        }

        return !mismatchesFound;
    }

    protected List<CheckUnmodifiableResult> checkUnmodifiableCollections(Object sourceObject, Object actualSourceObject, CheckExistingMetaInfo checkExistingMetaInfo) {
        List<CheckUnmodifiableResult> checkUnmodifiableResults = new ArrayList<>();

        boolean needsStop = false;

        for (UnmodifiedCollectionMetaInfo unmodifiedCollectionMetaInfo : checkExistingMetaInfo.unmodifiedCollectionMetaInfo) {
            if (needsStop) {
                break;
            }

            Collection<?> collection = (Collection<?>) unmodifiedCollectionMetaInfo.pathToCollection.getValueFromObject(sourceObject);
            Collection<?> actualCollection = (Collection<?>) unmodifiedCollectionMetaInfo.pathToCollection.getValueFromObject(actualSourceObject);

            if (!unmodifiedCollectionMetaInfo.fieldsForMatching.isEmpty()) {

                Map<String, MatchingElement> collectionByKeyMap;
                try {
                    collectionByKeyMap = collection.stream().collect(Collectors.toMap((o) -> getKeyString(o, unmodifiedCollectionMetaInfo), (o) -> new MatchingElement(o, false)));
                } catch (IllegalStateException illegalStateException) {
                    throw new RuntimeException("Collection has some duplicates by key fields", illegalStateException);
                }

                for (Object actualObj : actualCollection) {
                    String objectKeyString = getKeyString(actualObj, unmodifiedCollectionMetaInfo);

                    MatchingElement matched = collectionByKeyMap.get(objectKeyString);
                    if (matched == null) {
                        if (unmodifiedCollectionMetaInfo.forbiddenOperations.contains(CollectionOperation.REMOVE)) {
                            checkUnmodifiableResults.add(new CheckUnmodifiableResult(unmodifiedCollectionMetaInfo.pathToCollection,
                                    null,
                                    actualObj,
                                    unmodifiedCollectionMetaInfo.message,
                                    unmodifiedCollectionMetaInfo.collectionErrorMessageNaming));

                            if (stopUnmodifiableCheckOnFirstMismatch) {
                                needsStop = true;
                                break;
                            }
                        }
                    } else {
                        matched.setMatchWasFound(true);

                        if (unmodifiedCollectionMetaInfo.forbiddenOperations.contains(CollectionOperation.UPDATE)) {
                            boolean isEquals = unmodifiedCollectionMetaInfo.equalsFields.isEmpty() ? checkObjects(matched.elementValue, actualObj, unmodifiedCollectionMetaInfo.equalsMethod) : checkObjects(matched.elementValue, actualObj, unmodifiedCollectionMetaInfo.equalsFields);

                            if (!isEquals) {
                                checkUnmodifiableResults.add(new CheckUnmodifiableResult(unmodifiedCollectionMetaInfo.pathToCollection,
                                        matched.elementValue,
                                        actualObj,
                                        unmodifiedCollectionMetaInfo.message,
                                        unmodifiedCollectionMetaInfo.collectionErrorMessageNaming));

                                if (stopUnmodifiableCheckOnFirstMismatch) {
                                    needsStop = true;
                                    break;
                                }
                            }
                        }
                    }
                }

                if (unmodifiedCollectionMetaInfo.forbiddenOperations.contains(CollectionOperation.ADD)) {
                    if (!needsStop) {
                        for (MatchingElement matchingElement : collectionByKeyMap.values()) {
                            if (!matchingElement.matchWasFound) {
                                checkUnmodifiableResults.add(new CheckUnmodifiableResult(unmodifiedCollectionMetaInfo.pathToCollection,
                                        matchingElement.elementValue,
                                        null,
                                        unmodifiedCollectionMetaInfo.message,
                                        unmodifiedCollectionMetaInfo.collectionErrorMessageNaming));

                                if (stopUnmodifiableCheckOnFirstMismatch) {
                                    needsStop = true;
                                    break;
                                }
                            }
                        }
                    }
                }
            } else {
                List<Object> list = new ArrayList<>(collection);
                List<Object> actualList = new ArrayList<>(actualCollection);

                if (unmodifiedCollectionMetaInfo.forbiddenOperations.contains(CollectionOperation.UPDATE)) {
                    for (int i = 0; i < list.size(); i++) {
                        if (i >= actualList.size()) {
                            break;
                        }

                        Object obj = list.get(i);
                        Object actualObj = actualList.get(i);

                        boolean isEquals = unmodifiedCollectionMetaInfo.equalsFields.isEmpty() ? checkObjects(obj, actualObj, unmodifiedCollectionMetaInfo.equalsMethod) : checkObjects(obj, actualObj, unmodifiedCollectionMetaInfo.equalsFields);

                        if (!isEquals) {
                            checkUnmodifiableResults.add(new CheckUnmodifiableResult(unmodifiedCollectionMetaInfo.pathToCollection, obj, actualObj, unmodifiedCollectionMetaInfo.message, unmodifiedCollectionMetaInfo.collectionErrorMessageNaming));
                            if (this.stopUnmodifiableCheckOnFirstMismatch) {
                                needsStop = true;
                                break;
                            }
                        }
                    }
                }

                if (unmodifiedCollectionMetaInfo.forbiddenOperations.contains(CollectionOperation.ADD)) {
                    if (!(needsStop) && list.size() > actualList.size()) {
                        for (int i = actualList.size(); i < list.size(); i++) {
                            checkUnmodifiableResults.add(new CheckUnmodifiableResult(unmodifiedCollectionMetaInfo.pathToCollection, list.get(i), null, unmodifiedCollectionMetaInfo.message, unmodifiedCollectionMetaInfo.collectionErrorMessageNaming));
                        }

                        if (this.stopUnmodifiableCheckOnFirstMismatch) {
                            needsStop = true;
                        }
                    }
                }


                if (unmodifiedCollectionMetaInfo.forbiddenOperations.contains(CollectionOperation.REMOVE)) {
                    if (!(needsStop) && actualList.size() > list.size()) {
                        for (int i = list.size(); i < actualList.size(); i++) {
                            checkUnmodifiableResults.add(new CheckUnmodifiableResult(unmodifiedCollectionMetaInfo.pathToCollection, null, actualList.get(i), unmodifiedCollectionMetaInfo.message, unmodifiedCollectionMetaInfo.collectionErrorMessageNaming));
                        }

                        if (this.stopUnmodifiableCheckOnFirstMismatch) {
                            needsStop = true;
                        }
                    }
                }
            }
        }

        return checkUnmodifiableResults;
    }

    private String getKeyString(Object object, UnmodifiedCollectionMetaInfo unmodifiedCollectionMetaInfo) {
        if (object == null) {
            return "null";
        }

        StringBuilder keyString = new StringBuilder();
        for (FieldPath fieldPath : unmodifiedCollectionMetaInfo.fieldsForMatching) {
            Object fieldVal = fieldPath.getValueFromObject(object);
            keyString.append(fieldVal);
        }

        return keyString.toString();
    }


    protected List<CheckUnmodifiableResult> checkUnmodifiableAttributes(Object sourceObject, Object actualSourceObject, CheckExistingMetaInfo checkExistingMetaInfo) {
        List<CheckUnmodifiableResult> checkResult = new ArrayList<>();

        for (UnmodifiedAttributeMetaInfo unmodifiedAttr : checkExistingMetaInfo.unmodifiedAttributeMetaInfo) {
            Object attrVal = unmodifiedAttr.fieldPath.getValueFromObject(sourceObject);
            Object oldAttrVal = unmodifiedAttr.fieldPath.getValueFromObject(actualSourceObject);

            boolean isEquals = unmodifiedAttr.equalsFields.isEmpty() ? checkObjects(attrVal, oldAttrVal, unmodifiedAttr.equalsMethod) : checkObjects(attrVal, oldAttrVal, unmodifiedAttr.equalsFields);

            if (!isEquals) {
                checkResult.add(new CheckUnmodifiableResult(unmodifiedAttr.fieldPath, attrVal, oldAttrVal, unmodifiedAttr.message, unmodifiedAttr.attributeErrorMessageNaming));

                if (stopUnmodifiableCheckOnFirstMismatch) {
                    break;
                }
            }
        }

        return checkResult;
    }

    private boolean checkObjects(Object firstObject, Object secondObject, Set<FieldPath> equalsFields) {
        for (FieldPath equalsField : equalsFields) {
            Object valFromFirst = equalsField.getValueFromObject(firstObject);
            Object valFromSecond = equalsField.getValueFromObject(secondObject);

            if (!Objects.equals(valFromFirst, valFromSecond)) {
                return false;
            }
        }

        return true;
    }

    private boolean checkObjects(Object firstObject, Object secondObject, Method equalsMethod) {
        if (firstObject == secondObject) {
            return true;
        }

        if (secondObject != null) {
            Boolean result = (Boolean) ReflectionUtils.invokeMethod(equalsMethod, secondObject, firstObject);
            return result != null && result;
        }

        return false;
    }

    protected void fillContextValidatorForCheckUnmodifiableResult(ConstraintValidatorContext context, List<CheckUnmodifiableResult> checkUnmodifiableResults) {
        HibernateConstraintValidatorContext constraintValidatorContext = context.unwrap(HibernateConstraintValidatorContext.class);
        constraintValidatorContext.disableDefaultConstraintViolation();

        for (CheckUnmodifiableResult checkUnmodifiableResult : checkUnmodifiableResults) {
            constraintValidatorContext.addMessageParameter(FIELD_DIFF_NAME_PARAM_NAME, checkUnmodifiableResult.errorMessageNaming.isEmpty() ? checkUnmodifiableResult.fieldPath.path() : checkUnmodifiableResult.errorMessageNaming)
                    .addMessageParameter(FIELD_DIFF_VALUE_NEW_PARAM_NAME, checkUnmodifiableResult.newValue)
                    .addMessageParameter(FIELD_DIFF_VALUE_OLD_PARAM_NAME, checkUnmodifiableResult.oldValue);

            constraintValidatorContext.buildConstraintViolationWithTemplate(checkUnmodifiableResult.message).addConstraintViolation();
        }
    }

    protected void fillContextValidatorForExistingConstraint(ConstraintValidatorContext context, Set<FieldPath> constraintKey, Object object) {
        HibernateConstraintValidatorContext constraintValidatorContext = context.unwrap(HibernateConstraintValidatorContext.class);
        HibernateConstraintValidationUtils.fieldNameBatchesConstraintViolationBuild(constraintValidatorContext, constraintKey, object, DOESNT_EXIST_FIELDS_PARAM_NAME, DOESNT_EXIST_FIELD_VALUES_PARAM_NAME, message);
    }

    protected CriteriaQuery<Object> createCriteriaQuery(Class<?> clazz, Set<FieldPath> constraintKey, Object object) {
        CriteriaBuilder cb = em.getCriteriaBuilder();

        CriteriaQuery<Object> criteriaQuery = cb.createQuery(Object.class);
        Root<?> root = criteriaQuery.from(clazz);

        Predicate predicate = toEqualsPredicate(constraintKey, root, cb, object);

        if (!loadByConstraint) {
            criteriaQuery.select(cb.literal(true));
        }

        criteriaQuery.where(predicate);

        return criteriaQuery;
    }

    @Override
    protected void extractAndCashMetaDataForClass(Class<?> clazz) {
        Set<FieldPath> fieldsMetaInfoResult = extractExistingConstraintInfo(clazz);
        Set<UnmodifiedAttributeMetaInfo> unmodifiedAttributeMetaInfos = extractUnmodifiedAttributeMetaInfo(clazz);
        Set<UnmodifiedCollectionMetaInfo> unmodifiedCollectionMetaInfos = extractUnmodifiedCollectionMetaInfo(clazz);

        if (!(loadByConstraint) && (unmodifiableAttributes.length > 0 || unmodifiableCollections.length > 0)) {
            throw new IllegalArgumentException("Loading is necessary for unmodifiable check");
        }

        META_INFO.put(clazz, new CheckExistingMetaInfo(fieldsMetaInfoResult, unmodifiedAttributeMetaInfos, unmodifiedCollectionMetaInfos));
    }

    private Set<UnmodifiedCollectionMetaInfo> extractUnmodifiedCollectionMetaInfo(Class<?> clazz) {
        Set<UnmodifiedCollectionMetaInfo> unmodifiedCollections = new OrderedHashSet<>();

        for (UnmodifiableCollection unmodifiableCollection : unmodifiableCollections) {
            FieldPath collection = MetaInfoExtractorUtil.extractFieldPathMetaInfo(unmodifiableCollection.value(), clazz);
            Class<?> attributeClass = collection.getLast().clazz();

            if (!Collection.class.isAssignableFrom(attributeClass)) {
                throw new IllegalArgumentException("Class of field is not Collection");
            }

            Set<FieldPath> equalsFields = new OrderedHashSet<>();

            Arrays.stream(unmodifiableCollection.equalsFields()).forEach(equalsField -> equalsFields.add(MetaInfoExtractorUtil.extractFieldPathMetaInfo(equalsField, unmodifiableCollection.collectionGenericClass())));

            Method equalsMethod = getEqualsMethod(unmodifiableCollection.collectionGenericClass(), unmodifiableCollection.equalsMethodName());

            if (equalsMethod == null) {
                throw new IllegalArgumentException("Equals method doesn't exist");
            }

            if (!equalsMethod.getReturnType().equals(boolean.class) && !equalsMethod.getReturnType().equals(Boolean.class)) {
                throw new IllegalArgumentException("Equals method must return boolean");
            }

            Set<FieldPath> fieldsForMatching = MetaInfoExtractorUtil.extractFieldPathsMetaInfo(unmodifiableCollection.fieldsForMatching(), unmodifiableCollection.collectionGenericClass());

            unmodifiedCollections.add(new UnmodifiedCollectionMetaInfo(collection,
                    equalsMethod,
                    fieldsForMatching,
                    unmodifiableCollection.collectionGenericClass(),
                    unmodifiableCollection.message(),
                    unmodifiableCollection.collectionErrorMessageNaming(),
                    equalsFields,
                    Set.of(unmodifiableCollection.forbiddenOperations())));
        }

        return unmodifiedCollections;
    }

    private Set<UnmodifiedAttributeMetaInfo> extractUnmodifiedAttributeMetaInfo(Class<?> clazz) {
        Set<UnmodifiedAttributeMetaInfo> result = new OrderedHashSet<>();

        for (UnmodifiableAttribute unmodifiableAttribute : unmodifiableAttributes) {
            FieldPath attributePath = MetaInfoExtractorUtil.extractFieldPathMetaInfo(unmodifiableAttribute.value(), clazz);
            Class<?> attributeClass = attributePath.getLast().clazz();

            Set<FieldPath> equalsFields = new OrderedHashSet<>();

            Arrays.stream(unmodifiableAttribute.equalsFields()).forEach(equalsField -> equalsFields.add(MetaInfoExtractorUtil.extractFieldPathMetaInfo(equalsField, attributeClass)));

            Method equalsMethod = getEqualsMethod(attributeClass, unmodifiableAttribute.equalsMethodName());

            if (equalsMethod == null) {
                throw new IllegalArgumentException("Equals method doesn't exist");
            }

            if (!equalsMethod.getReturnType().equals(boolean.class) && !equalsMethod.getReturnType().equals(Boolean.class)) {
                throw new IllegalArgumentException("Equals method must return boolean");
            }

            result.add(new UnmodifiedAttributeMetaInfo(attributePath, equalsMethod, unmodifiableAttribute.message(), unmodifiableAttribute.attributeErrorMessageNaming(), equalsFields));
        }

        return result;
    }

    private Method getEqualsMethod(Class<?> clazz, String methodName) {
        Method methodWithSameClassArg = ReflectionUtils.findMethod(clazz, methodName, clazz);

        if (methodWithSameClassArg == null) {
            return ReflectionUtils.findMethod(clazz, methodName, Object.class);
        }

        return methodWithSameClassArg;
    }

    private Set<FieldPath> extractExistingConstraintInfo(Class<?> clazz) {
        Set<FieldPath> fieldsMetaInfoResult = new OrderedHashSet<>();

        for (String validationField : constraintKeyFields) {
            FieldPath path = MetaInfoExtractorUtil.extractFieldPathMetaInfo(validationField, clazz);
            fieldsMetaInfoResult.add(path);
        }

        return fieldsMetaInfoResult;
    }



    private static class MatchingElement {
        private final Object elementValue;

        private boolean matchWasFound;


        public MatchingElement(Object elementValue, boolean matchWasFound) {
            this.elementValue = elementValue;
            this.matchWasFound = matchWasFound;
        }

        public void setMatchWasFound(boolean matchWasFound) {
            this.matchWasFound = matchWasFound;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            MatchingElement that = (MatchingElement) o;
            return matchWasFound == that.matchWasFound && Objects.equals(elementValue, that.elementValue);
        }

        @Override
        public int hashCode() {
            return Objects.hash(elementValue, matchWasFound);
        }
    }


    private record CheckUnmodifiableResult(
            FieldPath fieldPath,
            Object newValue,
            Object oldValue,

            String message,

            String errorMessageNaming
    ) {

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            CheckUnmodifiableResult that = (CheckUnmodifiableResult) o;
            return Objects.equals(fieldPath, that.fieldPath) && Objects.equals(newValue, that.newValue) && Objects.equals(oldValue, that.oldValue);
        }

        @Override
        public int hashCode() {
            return Objects.hash(fieldPath, newValue, oldValue);
        }
    }

    private record CheckExistingMetaInfo(
            Set<FieldPath> existingConstraint,
            Set<UnmodifiedAttributeMetaInfo> unmodifiedAttributeMetaInfo,
            Set<UnmodifiedCollectionMetaInfo> unmodifiedCollectionMetaInfo
    ) {

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            CheckExistingMetaInfo that = (CheckExistingMetaInfo) o;
            return Objects.equals(existingConstraint, that.existingConstraint) && Objects.equals(unmodifiedAttributeMetaInfo, that.unmodifiedAttributeMetaInfo);
        }

        @Override
        public int hashCode() {
            return Objects.hash(existingConstraint, unmodifiedAttributeMetaInfo);
        }
    }

    private record UnmodifiedCollectionMetaInfo(
        FieldPath pathToCollection,
        Method equalsMethod,
        Set<FieldPath> fieldsForMatching,

        Class<?> collectionGenericClass,

        String message,

        String collectionErrorMessageNaming,

        Set<FieldPath> equalsFields,

        Set<CollectionOperation> forbiddenOperations
    ) {

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            UnmodifiedCollectionMetaInfo that = (UnmodifiedCollectionMetaInfo) o;
            return Objects.equals(pathToCollection, that.pathToCollection) && Objects.equals(equalsMethod, that.equalsMethod) && Objects.equals(fieldsForMatching, that.fieldsForMatching);
        }

        @Override
        public int hashCode() {
            return Objects.hash(pathToCollection, equalsMethod, fieldsForMatching);
        }
    }

    private record UnmodifiedAttributeMetaInfo(
        FieldPath fieldPath,
        Method equalsMethod,

        String message,

        String attributeErrorMessageNaming,

        Set<FieldPath> equalsFields
        ) {

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            UnmodifiedAttributeMetaInfo that = (UnmodifiedAttributeMetaInfo) o;
            return Objects.equals(fieldPath, that.fieldPath) && Objects.equals(equalsMethod, that.equalsMethod);
        }

        @Override
        public int hashCode() {
            return Objects.hash(fieldPath, equalsMethod);
        }
    }
}
