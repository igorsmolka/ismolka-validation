package com.ismolka.validation.validator;

import com.ismolka.validation.constraints.LimitValidationConstraints;
import com.ismolka.validation.constraints.inner.LimitValidationConstraintGroup;
import com.ismolka.validation.utils.metainfo.FieldPath;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.antlr.v4.runtime.misc.OrderedHashSet;
import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintValidatorContextImpl;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class LimitValidationConstraintValidatorField extends AbstractDbFieldConstraintsValidator<LimitValidationConstraints, Object> implements ConstraintValidator<LimitValidationConstraints, Object> {

    private static final Map<Class<?>, LimitMetaInfo> META_INFO = new ConcurrentHashMap<>();
    private static final String LIMIT_VALUE_STR_PARAM_NAME = "limit";

    private LimitValidationConstraintGroup[] limitValidationConstraintGroups;

    @Override
    public void initialize(LimitValidationConstraints constraintAnnotation) {
        this.message = constraintAnnotation.message();
        this.limitValidationConstraintGroups = constraintAnnotation.limitValueConstraints();
    }


    @Override
    @Transactional(readOnly = true)
    public boolean isValid(Object value, ConstraintValidatorContext context, EntityManager em) {
        Class<?> clazz = value.getClass();

        if (!META_INFO.containsKey(clazz)) {
            extractAndCashMetaDataForClass(clazz);
        }

        LimitMetaInfo limitMetaInfo = META_INFO.get(clazz);

        if (limitMetaInfo.isEmpty()) {
            return true;
        }

        for (LimitValueMetaInfo limitValueMetaInfo : limitMetaInfo.limitValueMetaInfo()) {
            boolean isValid = isValid(value, context, limitValueMetaInfo.limit(), limitValueMetaInfo.constraintKeys(), em);

            if (!isValid) {
                return false;
            }
        }

        return true;
    }


    private boolean isValid(Object value, ConstraintValidatorContext context, int limit, Set<Set<FieldPath>> constraintKeys, EntityManager em) {
        return getValidationResult(createCriteriaQuery(value.getClass(), constraintKeys, value, em), value, context, constraintKeys, limit, em);
    }

    private boolean getValidationResult(CriteriaQuery<Object[]> criteriaQuery, Object value, ConstraintValidatorContext context, Set<Set<FieldPath>> constraintKeys, Integer limitIfUnique, EntityManager em) {
        TypedQuery<Object[]> query = em.createQuery(criteriaQuery);
        query.setMaxResults(limitIfUnique);

        List<Object[]> resultList = query.getResultList();

        boolean isLimitReached = resultList.size() == limitIfUnique;

        if (isLimitReached) {
            fillContextValidator(context, constraintKeys, value, resultList, limitIfUnique);
        }

        return !isLimitReached;
    }


    protected void fillContextValidator(ConstraintValidatorContext context, Set<Set<FieldPath>> metaInfoConstraintKeys, Object object, List<Object[]> equalityMatrix, int limit) {
        ConstraintValidatorContextImpl constraintValidatorContext = (ConstraintValidatorContextImpl) context;

        int columnIndex = 0;

        for (Set<FieldPath> constraintKey : metaInfoConstraintKeys) {
            for (Object[] row : equalityMatrix) {
                Boolean isNotUnique = (Boolean) row[columnIndex];

                if (isNotUnique) {
                    String fields = constraintKey.stream().map(FieldPath::path).collect(Collectors.joining(", "));
                    String values = constraintKey.stream().map(fieldMetaInfo -> String.valueOf(fieldMetaInfo.getValueFromObject(object))).collect(Collectors.joining(", "));

                    constraintValidatorContext.addMessageParameter(LIMIT_VALUE_STR_PARAM_NAME, limit);
                    constraintValidatorContext.addMessageParameter(CONSTRAINT_ERROR_FIELDS_PARAM_NAME, fields);
                    constraintValidatorContext.addMessageParameter(CONSTRAINT_ERROR_FIELDS_VALUES_PARAM_NAME, values);

                    constraintValidatorContext.buildConstraintViolationWithTemplate(message)
                            .addConstraintViolation();
                    break;
                }
            }

            columnIndex++;
        }
        constraintValidatorContext.addMessageParameter(LIMIT_VALUE_STR_PARAM_NAME, limit);
    }


    @Override
    protected void extractAndCashMetaDataForClass(Class<?> clazz) {
        try {
            Set<LimitValueMetaInfo> limitValueMetaInfo = extractMetaInfoForLimitValidationFields(clazz);
            META_INFO.put(clazz, new LimitMetaInfo(clazz, limitValueMetaInfo));
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
                                 Set<LimitValueMetaInfo> limitValueMetaInfo
    ) {

        public boolean isEmpty() {
            return limitValueMetaInfo.isEmpty();
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
