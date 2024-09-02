package com.ismolka.validation.validator;

import com.ismolka.validation.constraints.CheckUniqueAnnotationWithIgnoreMatchById;
import com.ismolka.validation.constraints.LimitValidationConstraints;
import com.ismolka.validation.constraints.UniqueValidationConstraints;
import com.ismolka.validation.utils.metainfo.DatabaseFieldMetaInfoExtractorUtil;
import com.ismolka.validation.utils.metainfo.DatabaseFieldPath;
import com.ismolka.validation.utils.metainfo.FieldPath;
import com.ismolka.validation.utils.metainfo.MetaInfoExtractorUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.antlr.v4.runtime.misc.OrderedHashSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class CheckUniqueAnnotationWithIgnoreMatchByIdValidatorField extends AbstractDbFieldConstraintsValidator<CheckUniqueAnnotationWithIgnoreMatchById, Object> implements ConstraintValidator<CheckUniqueAnnotationWithIgnoreMatchById, Object> {

    private static final Map<Class<?>, CheckUniqueAnnotationWithIgnoreMatchByIdMetaInfo> META_INFO = new ConcurrentHashMap<>();

    private static final Integer LIMIT_IF_UNIQUE = 1;

    @Override
    public void initialize(CheckUniqueAnnotationWithIgnoreMatchById constraintAnnotation) {
        this.message = constraintAnnotation.message();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context, EntityManager entityManager) {
        Class<?> clazz = value.getClass();

        if (!META_INFO.containsKey(clazz)) {
            extractAndCashMetaDataForClass(clazz);
        }

        CheckUniqueAnnotationWithIgnoreMatchByIdMetaInfo checkUniqueAnnotationWithIgnoreMatchByIdMetaInfo = META_INFO.get(clazz);

        if (checkUniqueAnnotationWithIgnoreMatchByIdMetaInfo.isEmpty()) {
            return true;
        }

        return isValidExcludingId(value, context, checkUniqueAnnotationWithIgnoreMatchByIdMetaInfo.extractedFromUniqueAnnotationMetaInfo, checkUniqueAnnotationWithIgnoreMatchByIdMetaInfo.classIdFields, entityManager);
    }

    @Override
    protected void extractAndCashMetaDataForClass(Class<?> clazz) {
        Set<Set<DatabaseFieldPath>> extractedFromUniqueAnnotationMetaInfo = new OrderedHashSet<>();

        if (clazz.isAnnotationPresent(UniqueValidationConstraints.class)) {
            extractedFromUniqueAnnotationMetaInfo.addAll(extractConstraintFieldsInfoByAnnotations(clazz, clazz.getAnnotation(UniqueValidationConstraints.class).constraintKeys()));
        }

        META_INFO.put(clazz, new CheckUniqueAnnotationWithIgnoreMatchByIdMetaInfo(clazz, extractedFromUniqueAnnotationMetaInfo, DatabaseFieldMetaInfoExtractorUtil.extractIdFieldPathsMetaInfo(clazz)));
    }

    private boolean isValidExcludingId(Object value, ConstraintValidatorContext context, Set<Set<DatabaseFieldPath>> constraintKeys, Set<DatabaseFieldPath> classIdFields, EntityManager em) {
        CriteriaQuery<Object[]> criteriaQuery = createCriteriaQuery(value.getClass(), constraintKeys, value, em);

        CriteriaBuilder cb = em.getCriteriaBuilder();

        criteriaQuery.where(cb.and(criteriaQuery.getRestriction(), createPredicateForExcludingId(value, classIdFields, cb, criteriaQuery.getRoots().stream().findFirst().orElse(null))));

        TypedQuery<Object[]> query = em.createQuery(criteriaQuery);
        query.setMaxResults(LIMIT_IF_UNIQUE);

        List<Object[]> resultList = query.getResultList();

        boolean isLimitReached = resultList.size() == LIMIT_IF_UNIQUE;
        if (isLimitReached) {
            fillContextValidator(context, constraintKeys, value, resultList);
        }

        return !isLimitReached;
    }

    private Predicate createPredicateForExcludingId(Object value, Set<DatabaseFieldPath> classIdFields, CriteriaBuilder cb, Root<?> root) {
        List<Predicate> predicates = new ArrayList<>();
        for (DatabaseFieldPath idField : classIdFields) {
            Object idValue = idField.getValueFromObject(value);
            predicates.add(cb.notEqual(asPersistencePath(root, idField), idValue));
        }

        return cb.and(predicates.toArray(Predicate[]::new));
    }

    private record CheckUniqueAnnotationWithIgnoreMatchByIdMetaInfo(
            Class<?> clazz,
            Set<Set<DatabaseFieldPath>> extractedFromUniqueAnnotationMetaInfo,
            Set<DatabaseFieldPath> classIdFields
    ) {

        public boolean isEmpty() {
            return extractedFromUniqueAnnotationMetaInfo().isEmpty();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            CheckUniqueAnnotationWithIgnoreMatchByIdMetaInfo that = (CheckUniqueAnnotationWithIgnoreMatchByIdMetaInfo) o;
            return Objects.equals(extractedFromUniqueAnnotationMetaInfo, that.extractedFromUniqueAnnotationMetaInfo) && Objects.equals(classIdFields, that.classIdFields);
        }

        @Override
        public int hashCode() {
            return Objects.hash(extractedFromUniqueAnnotationMetaInfo, classIdFields);
        }
    }
}
