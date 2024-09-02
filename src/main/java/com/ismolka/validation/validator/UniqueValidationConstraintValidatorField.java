package com.ismolka.validation.validator;

import com.ismolka.validation.constraints.UniqueValidationConstraints;
import com.ismolka.validation.constraints.inner.ConstraintKey;
import com.ismolka.validation.utils.metainfo.DatabaseFieldPath;
import com.ismolka.validation.utils.metainfo.FieldPath;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class UniqueValidationConstraintValidatorField extends AbstractDbFieldConstraintsValidator<UniqueValidationConstraints, Object> implements ConstraintValidator<UniqueValidationConstraints, Object> {

    private static final Map<Class<?>, Set<Set<DatabaseFieldPath>>> META_INFO = new ConcurrentHashMap<>();

    private static final Integer MAX_RESULTS = 1;

    private ConstraintKey[] constraintKeys;

    @Override
    public void initialize(UniqueValidationConstraints constraintAnnotation) {
        this.message = constraintAnnotation.message();
        this.constraintKeys = constraintAnnotation.constraintKeys();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isValid(Object value, ConstraintValidatorContext context, EntityManager em) {
        Class<?> clazz = value.getClass();

        if (!META_INFO.containsKey(clazz)) {
            extractAndCashMetaDataForClass(clazz);
        }

        Set<Set<DatabaseFieldPath>> metaInfoConstraintKeys = META_INFO.get(clazz);

        TypedQuery<Object[]> query = em.createQuery(createCriteriaQuery(clazz, metaInfoConstraintKeys, value, em));
        query.setMaxResults(MAX_RESULTS);

        List<Object[]> resultList = query.getResultList();

        boolean isEmpty = resultList.isEmpty();

        if (!isEmpty) {
            fillContextValidator(context, metaInfoConstraintKeys, value, resultList);
        }

        return isEmpty;
    }


    @Override
    protected void extractAndCashMetaDataForClass(Class<?> clazz) {
        try {
            META_INFO.put(clazz, extractConstraintFieldsInfoByAnnotations(clazz, constraintKeys));
        } catch (Exception exc) {
            throw new RuntimeException(exc);
        }
    }
}
