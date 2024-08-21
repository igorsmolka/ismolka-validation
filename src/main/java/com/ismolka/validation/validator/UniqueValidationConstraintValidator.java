package com.ismolka.validation.validator;

import com.ismolka.validation.constraints.UniqueValidationConstraints;
import com.ismolka.validation.constraints.inner.ConstraintKey;
import com.ismolka.validation.validator.metainfo.FieldPath;
import jakarta.persistence.TypedQuery;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class UniqueValidationConstraintValidator extends AbstractDbConstraintsValidator<UniqueValidationConstraints, Object> implements ConstraintValidator<UniqueValidationConstraints, Object> {

    private static final Map<Class<?>, Set<Set<FieldPath>>> META_INFO = new ConcurrentHashMap<>();

    private static final Integer MAX_RESULTS = 1;

    private ConstraintKey[] constraintKeys;

    @Override
    public void initialize(UniqueValidationConstraints constraintAnnotation) {
        this.message = constraintAnnotation.message();
        this.constraintKeys = constraintAnnotation.constraintKeys();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        Class<?> clazz = value.getClass();

        if (!META_INFO.containsKey(clazz)) {
            extractAndCashMetaDataForClass(clazz);
        }

        Set<Set<FieldPath>> metaInfoConstraintKeys = META_INFO.get(clazz);

        TypedQuery<Object[]> query = em.createQuery(createCriteriaQuery(clazz, metaInfoConstraintKeys, value));
        query.setMaxResults(MAX_RESULTS);

        List<Object[]> resultList = query.getResultList();

        boolean isEmpty = resultList.isEmpty();

        if (!isEmpty) {
            fillContextValidator(context, metaInfoConstraintKeys, value, resultList);
        }

        return isEmpty;
    }


    protected void fillContextValidator(ConstraintValidatorContext context, Set<Set<FieldPath>> metaInfoConstraintKeys, Object object, List<Object[]> equalityMatrix) {
        HibernateConstraintValidatorContext constraintValidatorContext = context.unwrap(HibernateConstraintValidatorContext.class);

        int columnIndex = 0;

        for (Set<FieldPath> constraintKey : metaInfoConstraintKeys) {
            for (Object[] row : equalityMatrix) {
                Boolean isNotUnique = (Boolean) row[columnIndex];

                if (isNotUnique) {
                    String fields = constraintKey.stream().map(FieldPath::path).collect(Collectors.joining(", "));
                    String values = constraintKey.stream().map(fieldMetaInfo -> String.valueOf(fieldMetaInfo.getValueFromObject(object))).collect(Collectors.joining(", "));

                    constraintValidatorContext.addMessageParameter(CONSTRAINT_ERROR_FIELDS_PARAM_NAME, fields);
                    constraintValidatorContext.addMessageParameter(CONSTRAINT_ERROR_FIELDS_VALUES_PARAM_NAME, values);

                    constraintValidatorContext.buildConstraintViolationWithTemplate(message)
                            .addConstraintViolation();
                    break;
                }
            }

            columnIndex++;
        }
    }


    private void extractAndCashMetaDataForClass(Class<?> clazz) {
        try {
            META_INFO.put(clazz, extractConstraintFieldsInfoByAnnotations(clazz, constraintKeys));
        } catch (Exception exc) {
            throw new RuntimeException(exc);
        }
    }
}
