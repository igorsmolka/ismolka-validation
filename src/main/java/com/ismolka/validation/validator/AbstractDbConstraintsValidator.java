package com.ismolka.validation.validator;

import com.ismolka.validation.constraints.inner.ConstraintKey;
import com.ismolka.validation.validator.metainfo.FieldPath;
import com.ismolka.validation.validator.utils.MetaInfoExtractorUtil;
import jakarta.persistence.criteria.*;
import jakarta.validation.ConstraintValidator;
import org.antlr.v4.runtime.misc.OrderedHashSet;

import java.lang.annotation.Annotation;
import java.util.*;

public abstract class AbstractDbConstraintsValidator<T extends Annotation, A> extends AbstractEntityManagerConstraintValidator<T, A> implements ConstraintValidator<T, A> {

    protected static final String CONSTRAINT_ERROR_FIELDS_PARAM_NAME = "constraintErrorFields";

    protected static final String CONSTRAINT_ERROR_FIELDS_VALUES_PARAM_NAME = "constraintErrorFieldsValues";

    protected CriteriaQuery<Object[]> createCriteriaQuery(Class<?> clazz, Set<Set<FieldPath>> metaInfoConstraintKeys, Object object) {
        CriteriaBuilder cb = em.getCriteriaBuilder();

        CriteriaQuery<Object[]> criteriaQuery = cb.createQuery(Object[].class);
        Root<?> root = criteriaQuery.from(clazz);

        Set<Predicate> constraintsAsPredicates = constraintsAsPredicates(metaInfoConstraintKeys, root, cb, object);

        criteriaQuery.multiselect(equalityMatrixSelectionSet(constraintsAsPredicates, cb).stream().toList());
        criteriaQuery.where(cb.or(constraintsAsPredicates.toArray(Predicate[]::new)));

        return criteriaQuery;
    }

    protected Set<Selection<?>> equalityMatrixSelectionSet(Set<Predicate> constraintsAsPredicates, CriteriaBuilder cb) {
        Set<Selection<?>> resultSet = new OrderedHashSet<>();

        int aliasNum = 0;

        for (Predicate constraintPredicate : constraintsAsPredicates) {
            resultSet.add(cb.selectCase()
                    .when(cb.isTrue(constraintPredicate.as(Boolean.class)), cb.literal(true))
                    .otherwise(cb.literal(false)).alias("c" + aliasNum));

            aliasNum++;
        }

        return  resultSet;
    }



    protected Set<Set<FieldPath>> extractConstraintFieldsInfoByAnnotations(Class<?> clazz, ConstraintKey[] constraintKeys) {
        Set<Set<FieldPath>> result = new OrderedHashSet<>();

        for (ConstraintKey constraintKey : constraintKeys) {
            Set<FieldPath> fieldsMetaInfoResult = new OrderedHashSet<>();

            for (String validationField : constraintKey.value()) {
                FieldPath path = MetaInfoExtractorUtil.extractFieldPathMetaInfo(validationField, clazz);

                if (path.needsJoin()) {
                    throw new IllegalArgumentException(String.format("Joins not supported in such validators, fieldPath %s, class %s, validator %s", path.path(), clazz, this.getClass()));
                }

                fieldsMetaInfoResult.add(MetaInfoExtractorUtil.extractFieldPathMetaInfo(validationField, clazz));
            }

            result.add(fieldsMetaInfoResult);
        }

        return result;
    }

    protected Set<Predicate> constraintsAsPredicates(Set<Set<FieldPath>> metaInfoConstraintKeys, Root<?> root, CriteriaBuilder cb, Object obj) {
        Set<Predicate> allPredicates = new OrderedHashSet<>();

        for (Set<FieldPath> metaInfoConstraintKey : metaInfoConstraintKeys) {
            allPredicates.add(toEqualsPredicate(metaInfoConstraintKey, root, cb, obj));
        }

        return allPredicates;
    }
}
