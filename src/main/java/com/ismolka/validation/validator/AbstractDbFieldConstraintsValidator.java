package com.ismolka.validation.validator;

import com.ismolka.validation.constraints.inner.ConstraintKey;
import com.ismolka.validation.utils.metainfo.DatabaseFieldMetaInfoExtractorUtil;
import com.ismolka.validation.utils.metainfo.DatabaseFieldPath;
import com.ismolka.validation.utils.metainfo.FieldPath;
import com.ismolka.validation.utils.metainfo.MetaInfoExtractorUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.*;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.antlr.v4.runtime.misc.OrderedHashSet;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.stream.Collectors;

public abstract class AbstractDbFieldConstraintsValidator<T extends Annotation, A> extends AbstractEntityManagerConstraintValidator<T, A> implements ConstraintValidator<T, A> {

    protected static final String CONSTRAINT_ERROR_FIELDS_PARAM_NAME = "constraintErrorFields";

    protected static final String CONSTRAINT_ERROR_FIELDS_VALUES_PARAM_NAME = "constraintErrorFieldsValues";

    protected CriteriaQuery<Object[]> createCriteriaQuery(Class<?> clazz, Set<Set<DatabaseFieldPath>> metaInfoConstraintKeys, Object object, EntityManager em) {
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

    protected void fillContextValidator(ConstraintValidatorContext context, Set<Set<DatabaseFieldPath>> metaInfoConstraintKeys, Object object, List<Object[]> equalityMatrix) {
        HibernateConstraintValidatorContext constraintValidatorContext = context.unwrap(HibernateConstraintValidatorContext.class);

        int columnIndex = 0;

        for (Set<DatabaseFieldPath> constraintKey : metaInfoConstraintKeys) {
            for (Object[] row : equalityMatrix) {
                Boolean isNotUnique = (Boolean) row[columnIndex];

                if (isNotUnique) {
                    String fields = constraintKey.stream().map(DatabaseFieldPath::path).collect(Collectors.joining(", "));
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

    protected Set<Set<DatabaseFieldPath>> extractConstraintFieldsInfoByAnnotations(Class<?> clazz, ConstraintKey[] constraintKeys) {
        Set<Set<DatabaseFieldPath>> result = new OrderedHashSet<>();

        for (ConstraintKey constraintKey : constraintKeys) {
            Set<DatabaseFieldPath> fieldsMetaInfoResult = new OrderedHashSet<>();

            for (String validationField : constraintKey.value()) {
                DatabaseFieldPath path = DatabaseFieldMetaInfoExtractorUtil.extractDatabaseFieldPathMetaInfo(validationField, clazz);

                if (path.needsJoin()) {
                    throw new IllegalArgumentException(String.format("Joins not supported in such validators, attribute %s, class %s, validator %s", path.path(), clazz, this.getClass()));
                }

                fieldsMetaInfoResult.add(path);
            }

            result.add(fieldsMetaInfoResult);
        }

        return result;
    }

    protected Set<Predicate> constraintsAsPredicates(Set<Set<DatabaseFieldPath>> metaInfoConstraintKeys, Root<?> root, CriteriaBuilder cb, Object obj) {
        Set<Predicate> allPredicates = new OrderedHashSet<>();

        for (Set<DatabaseFieldPath> metaInfoConstraintKey : metaInfoConstraintKeys) {
            allPredicates.add(toEqualsPredicate(metaInfoConstraintKey, root, cb, obj));
        }

        return allPredicates;
    }
}
