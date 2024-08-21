package com.ismolka.validation.validator;

import com.ismolka.validation.constraints.LimitValidationConstraints;
import com.ismolka.validation.constraints.UniqueValidationConstraints;
import com.ismolka.validation.constraints.inner.LimitValidationConstraintGroup;
import com.ismolka.validation.validator.metainfo.FieldPath;
import jakarta.persistence.TypedQuery;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.antlr.v4.runtime.misc.OrderedHashSet;
import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintValidatorContextImpl;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class LimitValidationConstraintValidator extends AbstractDbConstraintsValidator<LimitValidationConstraints, Object> implements ConstraintValidator<LimitValidationConstraints, Object> {

    private static final Map<Class<?>, LimitMetaInfo> META_INFO = new ConcurrentHashMap<>();
    private static final String LIMIT_VALUE_STR_PARAM_NAME = "limit";

    private static final int LIMIT_FOR_UNIQUE_ANNOTATION = 2;

    private LimitValidationConstraintGroup[] limitValidationConstraintGroups;

    private boolean alsoCheckUniqueAnnotation;

    @Override
    public void initialize(LimitValidationConstraints constraintAnnotation) {
        this.message = constraintAnnotation.message();
        this.limitValidationConstraintGroups = constraintAnnotation.limitValueConstraints();
        this.alsoCheckUniqueAnnotation = constraintAnnotation.alsoCheckByUniqueAnnotationWithIgnoringOneMatch();
    }


    @Override
    @Transactional(readOnly = true)
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        Class<?> clazz = value.getClass();

        if (!META_INFO.containsKey(clazz)) {
            extractAndCashMetaDataForClass(clazz);
        }

        LimitMetaInfo limitMetaInfo = META_INFO.get(clazz);

        if (limitMetaInfo.isEmpty()) {
            return true;
        }

        for (LimitValueMetaInfo limitValueMetaInfo : limitMetaInfo.limitValueMetaInfo()) {
            boolean isValid = isValid(value, context, limitValueMetaInfo.limit(), limitValueMetaInfo.constraintKeys());

            if (!isValid) {
                return false;
            }
        }

        if (this.alsoCheckUniqueAnnotation && !limitMetaInfo.extractedFromUniqueAnnotationMetaInfo.isEmpty()) {
            return isValid(value, context, LIMIT_FOR_UNIQUE_ANNOTATION, limitMetaInfo.extractedFromUniqueAnnotationMetaInfo);
        }

        return true;
    }


    private boolean isValid(Object value, ConstraintValidatorContext context, int limit, Set<Set<FieldPath>> constraintKeys) {
        TypedQuery<Object[]> query = em.createQuery(createCriteriaQuery(value.getClass(), constraintKeys, value));
        query.setMaxResults(limit);

        List<Object[]> resultList = query.getResultList();

        boolean isLimitReached = resultList.size() == limit;

        if (isLimitReached) {
            fillContextValidator(context, constraintKeys, value, resultList, limit);
        }

        return !isLimitReached;
    }

    protected void fillContextValidator(ConstraintValidatorContext context, Set<Set<FieldPath>> metaInfoConstraintKeys, Object object, List<Object[]> equalityMatrix, int limit) {
        ConstraintValidatorContextImpl constraintValidatorContext = (ConstraintValidatorContextImpl) context;

        super.fillContextValidator(context, metaInfoConstraintKeys, object, equalityMatrix);
        constraintValidatorContext.addMessageParameter(LIMIT_VALUE_STR_PARAM_NAME, limit);
    }



    private void extractAndCashMetaDataForClass(Class<?> clazz) {
        try {
            Set<LimitValueMetaInfo> limitValueMetaInfo = extractMetaInfoForLimitValidationFields(clazz);
            Set<Set<FieldPath>> extractedFromUniqueAnnotationMetaInfo = new OrderedHashSet<>();

            if (this.alsoCheckUniqueAnnotation) {
                if (clazz.isAnnotationPresent(UniqueValidationConstraints.class)) {
                    extractedFromUniqueAnnotationMetaInfo.addAll(extractConstraintFieldsInfoByAnnotations(clazz, clazz.getAnnotation(UniqueValidationConstraints.class).constraintKeys()));
                }
            }

            META_INFO.put(clazz, new LimitMetaInfo(clazz, limitValueMetaInfo, extractedFromUniqueAnnotationMetaInfo));
        } catch (Exception exc) {
            throw new RuntimeException(exc);
        }
    }

    private Set<LimitValueMetaInfo> extractMetaInfoForLimitValidationFields(Class<?> clazz) {
        Set<LimitValueMetaInfo> result = new OrderedHashSet<>();

        for (LimitValidationConstraintGroup limitValidationConstraintGroup : limitValidationConstraintGroups) {
            result.add(new LimitValueMetaInfo(limitValidationConstraintGroup.limit(), extractConstraintFieldsInfoByAnnotations(clazz, limitValidationConstraintGroup.constraintKeys())));
        }

        return result;
    }

    private record LimitMetaInfo(Class<?> clazz,
                                 Set<LimitValueMetaInfo> limitValueMetaInfo,
                                 Set<Set<FieldPath>> extractedFromUniqueAnnotationMetaInfo) {

        public boolean isEmpty() {
            return limitValueMetaInfo.isEmpty() && extractedFromUniqueAnnotationMetaInfo.isEmpty();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            LimitMetaInfo that = (LimitMetaInfo) o;
            return Objects.equals(clazz, that.clazz);
        }

        @Override
        public int hashCode() {
            return Objects.hash(clazz);
        }
    }

    private record LimitValueMetaInfo(int limit, Set<Set<FieldPath>> constraintKeys) {

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            LimitValueMetaInfo that = (LimitValueMetaInfo) o;
            return limit == that.limit && Objects.equals(constraintKeys, that.constraintKeys);
        }

        @Override
        public int hashCode() {
            return Objects.hash(limit, constraintKeys);
        }
    }
}
