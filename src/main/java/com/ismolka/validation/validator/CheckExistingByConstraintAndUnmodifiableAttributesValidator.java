package com.ismolka.validation.validator;

import com.ismolka.validation.constraints.CheckExistingByConstraintAndUnmodifiableAttributes;
import com.ismolka.validation.utils.change.collection.CollectionChangesChecker;
import com.ismolka.validation.utils.change.collection.CollectionChangesCheckerResult;
import com.ismolka.validation.utils.change.collection.CollectionElementDifference;
import com.ismolka.validation.utils.change.collection.DefaultCollectionChangesCheckerBuilder;
import com.ismolka.validation.utils.change.value.DefaultValueChangesCheckerBuilder;
import com.ismolka.validation.utils.change.value.ValueChangesChecker;
import com.ismolka.validation.utils.change.value.ValueChangesCheckerResult;
import com.ismolka.validation.utils.change.value.ValueDifference;
import com.ismolka.validation.utils.constant.CollectionOperation;
import com.ismolka.validation.constraints.inner.UnmodifiableAttribute;
import com.ismolka.validation.constraints.inner.UnmodifiableCollection;
import com.ismolka.validation.utils.metainfo.FieldPath;
import com.ismolka.validation.validator.utils.HibernateConstraintValidationUtils;
import com.ismolka.validation.utils.metainfo.MetaInfoExtractorUtil;
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

        for (UnmodifiedCollectionMetaInfo<?> unmodifiedCollectionMetaInfo : checkExistingMetaInfo.unmodifiedCollectionMetaInfo) {
            CollectionChangesCheckerResult<?> collectionChangesCheckerResult = getCollectionChangesCheckerResultForUnmodifiedCollection(unmodifiedCollectionMetaInfo, sourceObject, actualSourceObject);
            if (!collectionChangesCheckerResult.equalsResult()) {
                Map<CollectionOperation, Set<CollectionElementDifference<?>>> collectionDifferenceMap = (Map) collectionChangesCheckerResult.collectionDifferenceMap();

                if (collectionDifferenceMap.containsKey(CollectionOperation.ADD)) {
                    fillCheckUnmodifiableResultListForCollectionOperation(CollectionOperation.ADD, unmodifiedCollectionMetaInfo, collectionDifferenceMap, checkUnmodifiableResults);
                }

                if (collectionDifferenceMap.containsKey(CollectionOperation.REMOVE)) {
                    fillCheckUnmodifiableResultListForCollectionOperation(CollectionOperation.REMOVE, unmodifiedCollectionMetaInfo, collectionDifferenceMap, checkUnmodifiableResults);
                }

                if (collectionDifferenceMap.containsKey(CollectionOperation.UPDATE)) {
                    fillCheckUnmodifiableResultListForCollectionOperation(CollectionOperation.UPDATE, unmodifiedCollectionMetaInfo, collectionDifferenceMap, checkUnmodifiableResults);
                }
            }
        }

        return checkUnmodifiableResults;
    }

    private void fillCheckUnmodifiableResultListForCollectionOperation(CollectionOperation collectionOperation, UnmodifiedCollectionMetaInfo<?> unmodifiedCollectionMetaInfo, Map<CollectionOperation, Set<CollectionElementDifference<?>>> collectionDifferenceMap, List<CheckUnmodifiableResult> checkUnmodifiableResults) {
        Set<CollectionElementDifference<?>> addElementDiffs = collectionDifferenceMap.get(collectionOperation);
        for (CollectionElementDifference<?> collectionElementDiff : addElementDiffs) {
            checkUnmodifiableResults.add(new CheckUnmodifiableResult(unmodifiedCollectionMetaInfo.pathToCollection, collectionElementDiff.elementFromNewCollection(), collectionElementDiff.elementFromOldCollection(), unmodifiedCollectionMetaInfo.message, unmodifiedCollectionMetaInfo.collectionErrorMessageNaming));
        }
    }

    private <X> CollectionChangesCheckerResult<X> getCollectionChangesCheckerResultForUnmodifiedCollection(UnmodifiedCollectionMetaInfo<X> unmodifiedCollectionMetaInfo, Object sourceObject, Object actualSourceObject) {
        Collection<X> collection = (Collection<X>) unmodifiedCollectionMetaInfo.pathToCollection.getValueFromObject(sourceObject);
        Collection<X> actualCollection = (Collection<X>) unmodifiedCollectionMetaInfo.pathToCollection.getValueFromObject(actualSourceObject);

        return unmodifiedCollectionMetaInfo.collectionChangesChecker.getResult(actualCollection, collection);
    }

    protected List<CheckUnmodifiableResult> checkUnmodifiableAttributes(Object sourceObject, Object actualSourceObject, CheckExistingMetaInfo checkExistingMetaInfo) {
        List<CheckUnmodifiableResult> checkResult = new ArrayList<>();

        for (UnmodifiedAttributeMetaInfo<?> unmodifiedAttr : checkExistingMetaInfo.unmodifiedAttributeMetaInfo) {
            ValueChangesCheckerResult valueChangesCheckerResult = getValueChangesCheckerResultForUnmodifiedAttribute(unmodifiedAttr, sourceObject, actualSourceObject);
            if (!valueChangesCheckerResult.equalsResult()) {
                ValueDifference<?> valueDifference = valueChangesCheckerResult.differenceMap().get(unmodifiedAttr.fieldPath.path()).unwrap(ValueDifference.class);
                checkResult.add(new CheckUnmodifiableResult(unmodifiedAttr.fieldPath, valueDifference.newValue(), valueDifference.oldValue(), unmodifiedAttr.message, unmodifiedAttr.attributeErrorMessageNaming));

                if (stopUnmodifiableCheckOnFirstMismatch) {
                    break;
                }
            }
        }

        return checkResult;
    }

    @SuppressWarnings("uncheked")
    private <X> ValueChangesCheckerResult getValueChangesCheckerResultForUnmodifiedAttribute(UnmodifiedAttributeMetaInfo<X> unmodifiedAttr, Object sourceObject, Object actualSourceObject) {
        X attrVal = (X) unmodifiedAttr.fieldPath.getValueFromObject(sourceObject);
        X actualAttrVal = (X) unmodifiedAttr.fieldPath.getValueFromObject(actualSourceObject);

        return unmodifiedAttr.valueChangesChecker.getResult(actualAttrVal, attrVal);
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
        Set<UnmodifiedAttributeMetaInfo<?>> unmodifiedAttributeMetaInfos = extractUnmodifiedAttributeMetaInfo(clazz);
        Set<UnmodifiedCollectionMetaInfo<?>> unmodifiedCollectionMetaInfos = extractUnmodifiedCollectionMetaInfo(clazz);

        if (!(loadByConstraint) && (unmodifiableAttributes.length > 0 || unmodifiableCollections.length > 0)) {
            throw new IllegalArgumentException("Loading is necessary for unmodifiable check");
        }

        META_INFO.put(clazz, new CheckExistingMetaInfo(fieldsMetaInfoResult, unmodifiedAttributeMetaInfos, unmodifiedCollectionMetaInfos));
    }

    private Set<UnmodifiedCollectionMetaInfo<?>> extractUnmodifiedCollectionMetaInfo(Class<?> clazz) {
        Set<UnmodifiedCollectionMetaInfo<?>> unmodifiedCollections = new OrderedHashSet<>();

        for (UnmodifiableCollection unmodifiableCollection : unmodifiableCollections) {
            FieldPath collection = MetaInfoExtractorUtil.extractFieldPathMetaInfo(unmodifiableCollection.value(), clazz);

            unmodifiedCollections.add(createUnmodifiedCollectionMetaInfo(unmodifiableCollection.collectionGenericClass(), collection, unmodifiableCollection));
        }

        return unmodifiedCollections;
    }

    private <X> UnmodifiedCollectionMetaInfo<X> createUnmodifiedCollectionMetaInfo(Class<X> collectionClass, FieldPath collection, UnmodifiableCollection unmodifiableCollection) {
        Class<?> attributeClass = collection.getLast().clazz();

        if (!Collection.class.isAssignableFrom(attributeClass)) {
            throw new IllegalArgumentException(String.format("Class of field %s is not Collection", unmodifiableCollection.value()));
        }

        DefaultCollectionChangesCheckerBuilder<X> collectionChangesCheckerBuilder = DefaultCollectionChangesCheckerBuilder.builder(collectionClass);

        Arrays.stream(unmodifiableCollection.equalsFields()).forEach(collectionChangesCheckerBuilder::addGlobalEqualsField);

        if (!unmodifiableCollection.equalsMethodName().isBlank()) {
            collectionChangesCheckerBuilder.globalEqualsMethodReflection(getEqualsMethod(attributeClass, unmodifiableCollection.equalsMethodName()));
        }

        Arrays.stream(unmodifiableCollection.fieldsForMatching()).forEach(collectionChangesCheckerBuilder::addFieldForMatching);

        if (stopUnmodifiableCheckOnFirstMismatch) {
            collectionChangesCheckerBuilder.stopOnFirstDiff();
        }

        collectionChangesCheckerBuilder.forOperations(unmodifiableCollection.forbiddenOperations());

        return new UnmodifiedCollectionMetaInfo<>(collection,
                collectionClass,
                unmodifiableCollection.message(),
                unmodifiableCollection.collectionErrorMessageNaming(),
                Set.of(unmodifiableCollection.forbiddenOperations()),
                collectionChangesCheckerBuilder.build());
    }


    private Set<UnmodifiedAttributeMetaInfo<?>> extractUnmodifiedAttributeMetaInfo(Class<?> clazz) {
        Set<UnmodifiedAttributeMetaInfo<?>> result = new OrderedHashSet<>();

        for (UnmodifiableAttribute unmodifiableAttribute : unmodifiableAttributes) {
            FieldPath attributePath = MetaInfoExtractorUtil.extractFieldPathMetaInfo(unmodifiableAttribute.value(), clazz);
            Class<?> attributeClass = attributePath.getLast().clazz();

            result.add(createUnmodifiedAttributeMetaInfo(attributeClass, attributePath, unmodifiableAttribute));
        }

        return result;
    }

    private <X> UnmodifiedAttributeMetaInfo<X> createUnmodifiedAttributeMetaInfo(Class<X> attributeClass, FieldPath attributePath, UnmodifiableAttribute unmodifiableAttribute) {
        DefaultValueChangesCheckerBuilder<X> valueChangesCheckerBuilder = DefaultValueChangesCheckerBuilder.builder(attributeClass);
        Arrays.stream(unmodifiableAttribute.equalsFields()).forEach(valueChangesCheckerBuilder::addGlobalEqualsField);

        if (!unmodifiableAttribute.equalsMethodName().isBlank()) {
            valueChangesCheckerBuilder.globalEqualsMethodReflection(getEqualsMethod(attributeClass, unmodifiableAttribute.equalsMethodName()));
        }

        if (stopUnmodifiableCheckOnFirstMismatch) {
            valueChangesCheckerBuilder.stopOnFirstDiff();
        }

        return new UnmodifiedAttributeMetaInfo<>(attributeClass, attributePath, unmodifiableAttribute.message(), unmodifiableAttribute.attributeErrorMessageNaming(), valueChangesCheckerBuilder.build());
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
            Set<UnmodifiedAttributeMetaInfo<?>> unmodifiedAttributeMetaInfo,
            Set<UnmodifiedCollectionMetaInfo<?>> unmodifiedCollectionMetaInfo
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

    private record UnmodifiedCollectionMetaInfo<X>(
        FieldPath pathToCollection,

        Class<X> collectionGenericClass,

        String message,

        String collectionErrorMessageNaming,

        Set<CollectionOperation> forbiddenOperations,

        CollectionChangesChecker<X> collectionChangesChecker
    ) {

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            UnmodifiedCollectionMetaInfo<?> that = (UnmodifiedCollectionMetaInfo<?>) o;
            return Objects.equals(pathToCollection, that.pathToCollection);
        }

        @Override
        public int hashCode() {
            return Objects.hash(pathToCollection);
        }
    }

    private record UnmodifiedAttributeMetaInfo<X>(

        Class<X> attributeClass,

        FieldPath fieldPath,

        String message,

        String attributeErrorMessageNaming,

        ValueChangesChecker<X> valueChangesChecker
        ) {

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            UnmodifiedAttributeMetaInfo<?> that = (UnmodifiedAttributeMetaInfo<?>) o;
            return Objects.equals(fieldPath, that.fieldPath);
        }

        @Override
        public int hashCode() {
            return Objects.hash(fieldPath);
        }
    }
}
