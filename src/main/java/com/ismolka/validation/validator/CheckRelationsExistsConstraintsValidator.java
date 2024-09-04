package com.ismolka.validation.validator;

import com.ismolka.validation.constraints.CheckRelationsExistsConstraints;
import com.ismolka.validation.constraints.inner.RelationCheckConstraint;
import com.ismolka.validation.constraints.inner.RelationCheckConstraintFieldMapping;
import com.ismolka.validation.utils.metainfo.DatabaseFieldMetaInfo;
import com.ismolka.validation.utils.metainfo.DatabaseFieldMetaInfoExtractorUtil;
import com.ismolka.validation.utils.metainfo.DatabaseFieldPath;
import com.ismolka.validation.utils.metainfo.FieldPath;
import com.ismolka.validation.utils.metainfo.MetaInfoExtractorUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.antlr.v4.runtime.misc.OrderedHashSet;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class CheckRelationsExistsConstraintsValidator extends AbstractEntityManagerConstraintValidator<CheckRelationsExistsConstraints, Object> implements ConstraintValidator<CheckRelationsExistsConstraints, Object> {


    private static final Map<Class<?>, CheckRelationsMetaInfo> META_INFO = new ConcurrentHashMap<>();

    private static final String RELATION_DOESNT_EXIST_FIELD_PARAM_NAME = "relationDoesntExistField";

    private static final String RELATION_DOESNT_EXIST_FIELD_VALUE_PARAM_NAME = "relationDoesntExistFieldValue";

    private static final String RELATION_DOESNT_EXIST_NAME_PARAM_NAME = "relationDoesntExist";

    private static final Integer LIMIT = 1;

    private RelationCheckConstraint[] relationCheckConstraints;

    private boolean ignoreMainMessage;

    @Override
    public void initialize(CheckRelationsExistsConstraints constraintAnnotation) {
        this.message = constraintAnnotation.message();
        this.relationCheckConstraints = constraintAnnotation.value();
        this.ignoreMainMessage = !constraintAnnotation.addMessageToViolations();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isValid(Object value, ConstraintValidatorContext context, EntityManager em) {
        Class<?> clazz = value.getClass();

        if (!META_INFO.containsKey(clazz)) {
            extractAndCashMetaDataForClass(clazz);
        }

        CheckRelationsMetaInfo checkRelationsMetaInfo = META_INFO.get(clazz);

        TypedQuery<Object[]> query = em.createQuery(createCriteriaQuery(checkRelationsMetaInfo, value, em));
        query.setMaxResults(LIMIT);

        List<Object[]> resultList = query.getResultList();
        boolean somethingDoesntExist = Arrays.stream(resultList.get(0)).anyMatch(Objects::isNull);

        if (somethingDoesntExist) {
            fillContextValidator(context, checkRelationsMetaInfo, value, resultList.get(0));
        }

        return !somethingDoesntExist;
    }

    private void fillContextValidator(ConstraintValidatorContext context, CheckRelationsMetaInfo checkRelationsMetaInfo, Object object, Object[] existInfoArray) {
        HibernateConstraintValidatorContext constraintValidatorContext = context.unwrap(HibernateConstraintValidatorContext.class);

        int index = 0;

        if (ignoreMainMessage) {
            constraintValidatorContext.disableDefaultConstraintViolation();
        } else {
            constraintValidatorContext.buildConstraintViolationWithTemplate(message).addConstraintViolation();
        }

        for (CheckRelationMetaInfo checkRelationMetaInfo : checkRelationsMetaInfo.checkRelationMetaInfo) {
            Object existingInfo = existInfoArray[index];


            if (existingInfo == null) {
                List<String> notExistingFields = new ArrayList<>();
                List<String> valuesForFields = new ArrayList<>();

                String classStr = "";

                if (checkRelationMetaInfo.fromFkToPkFieldMapping.isEmpty()) {
                    Object source = checkRelationMetaInfo.relationField.getValueFromObject(object);
                    classStr = source.getClass().getSimpleName();

                    for (DatabaseFieldPath fieldPath : checkRelationMetaInfo.relationClassIds) {
                        notExistingFields.add(fieldPath.path());
                        valuesForFields.add(String.valueOf(fieldPath.getValueFromObject(source)));
                    }
                } else {
                    classStr = object.getClass().getSimpleName();

                    for (DatabaseFieldPath fieldPath : checkRelationMetaInfo.fromFkToPkFieldMapping.keySet()) {
                        notExistingFields.add(fieldPath.path());
                        valuesForFields.add(String.valueOf(fieldPath.getValueFromObject(object)));
                    }
                }

                String notExistingFieldsBatch = String.join(", ", notExistingFields);
                String notExistingValuesBatch = String.join(", ", valuesForFields);

                constraintValidatorContext.addMessageParameter(RELATION_DOESNT_EXIST_FIELD_PARAM_NAME, notExistingFieldsBatch)
                        .addMessageParameter(RELATION_DOESNT_EXIST_FIELD_VALUE_PARAM_NAME, notExistingValuesBatch)
                        .addMessageParameter(RELATION_DOESNT_EXIST_NAME_PARAM_NAME, checkRelationMetaInfo.relationErrorMessageNaming.isEmpty() ? classStr : checkRelationMetaInfo.relationErrorMessageNaming);

                constraintValidatorContext.buildConstraintViolationWithTemplate(checkRelationMetaInfo.message).addConstraintViolation();
            }

            index++;
        }
    }

    @Override
    protected void extractAndCashMetaDataForClass(Class<?> clazz) {
        Set<CheckRelationMetaInfo> checkRelationMetaInfo = new OrderedHashSet<>();

        for (RelationCheckConstraint relationCheckConstraint : relationCheckConstraints) {
            if (relationCheckConstraint.relationField().isEmpty()
                    && relationCheckConstraint.relationMapping().length == 0) {
                throw new IllegalArgumentException("Annotation RelationCheckConstraint is empty");
            }

            if (relationCheckConstraint.relationField().isEmpty() && relationCheckConstraint.relationClass() == Object.class) {
                throw new IllegalArgumentException("Can't provide a validation by relationFieldIds without any information about joined class");
            }

            DatabaseFieldPath relationFieldPath = null;
            if (!relationCheckConstraint.relationField().isEmpty()) {
                relationFieldPath = DatabaseFieldMetaInfoExtractorUtil.extractDatabaseFieldPathMetaInfo(relationCheckConstraint.relationField(), clazz);
            }

            DatabaseFieldMetaInfo firstJoinMetaInfo = null;
            if (relationFieldPath != null) {
                firstJoinMetaInfo = relationFieldPath.findFirstJoin();

                if (firstJoinMetaInfo.joinTable()) {
                    throw new IllegalArgumentException("Join tables are not supported");
                }
            }

            Class<?> relationClass = relationCheckConstraint.relationClass() != Object.class ? relationCheckConstraint.relationClass() : firstJoinMetaInfo.field().clazz();

            Map<DatabaseFieldPath, DatabaseFieldPath> fromFkToPkFieldMapping = new HashMap<>();
            if (relationCheckConstraint.relationMapping().length != 0) {
                for (RelationCheckConstraintFieldMapping relationCheckConstraintFieldMappingItem : relationCheckConstraint.relationMapping()) {
                    DatabaseFieldPath fkPath = DatabaseFieldMetaInfoExtractorUtil.extractDatabaseFieldPathMetaInfo(relationCheckConstraintFieldMappingItem.fromForeignKeyField(), clazz);

                    DatabaseFieldPath joinPkPath = DatabaseFieldMetaInfoExtractorUtil.extractDatabaseFieldPathMetaInfo(relationCheckConstraintFieldMappingItem.toPrimaryKeyField(), relationClass);

                    fromFkToPkFieldMapping.put(fkPath, joinPkPath);
                }
            }

            Set<DatabaseFieldPath> relationClassIds = DatabaseFieldMetaInfoExtractorUtil.extractIdFieldPathsMetaInfo(relationClass);

            checkRelationMetaInfo.add(new CheckRelationMetaInfo(relationFieldPath, fromFkToPkFieldMapping, relationClassIds, relationClass, relationCheckConstraint.message(), relationCheckConstraint.relationErrorMessageNaming(), relationCheckConstraint.nullable()));
        }

        META_INFO.put(clazz, new CheckRelationsMetaInfo(checkRelationMetaInfo));
    }

    private CriteriaQuery<Object[]> createCriteriaQuery(CheckRelationsMetaInfo checkRelationsMetaInfo, Object object, EntityManager em) {
        CriteriaBuilder cb = em.getCriteriaBuilder();

        CriteriaQuery<Object[]> criteriaQuery = cb.createQuery(Object[].class);

        List<Selection<?>> selectList = new ArrayList<>();

        checkRelationsMetaInfo.checkRelationMetaInfo().forEach(checkRelationMetaInfo -> {
            Subquery<Boolean> subquery = createSubQueryForCheckRelationMetaInfo(checkRelationMetaInfo, cb, criteriaQuery, object);
            selectList.add(subquery.as(Boolean.class));
        });

        criteriaQuery.multiselect(selectList);

        return criteriaQuery;
    }

    private Subquery<Boolean> createSubQueryForCheckRelationMetaInfo(CheckRelationMetaInfo checkRelationMetaInfo,
                                                                     CriteriaBuilder cb,
                                                                     CriteriaQuery<Object[]> criteriaQuery,
                                                                     Object object) {
        Subquery<Boolean> subQuery = criteriaQuery.subquery(Boolean.class);
        Root<?> subRoot = subQuery.from(checkRelationMetaInfo.relationClass);

        subQuery.select(cb.literal(true));
        Predicate predicateForCheckExistence = createPredicateByCheckRelationMetaInfo(checkRelationMetaInfo, subRoot, cb, object);
        if (predicateForCheckExistence != null) {
            subQuery.where(createPredicateByCheckRelationMetaInfo(checkRelationMetaInfo, subRoot, cb, object));
        }

        return subQuery;
    }

    private Predicate createPredicateByCheckRelationMetaInfo(CheckRelationMetaInfo checkRelationMetaInfo, Root<?> root, CriteriaBuilder cb, Object object) {
        if (checkRelationMetaInfo.fromFkToPkFieldMapping.isEmpty()) {
            Object source = checkRelationMetaInfo.relationField.getValueFromObject(object);

            if (source == null) {
                if (checkRelationMetaInfo.nullable) {
                    return null;
                } else {
                    throw new IllegalArgumentException("Relation is null");
                }
            }

            return toEqualsPredicate(checkRelationMetaInfo.relationClassIds, root, cb, source);

        } else {
            if (object == null) {
                throw new IllegalArgumentException("Object is null");
            }

            if (mappingFieldsAreNull(object, checkRelationMetaInfo.fromFkToPkFieldMapping)) {
                if (checkRelationMetaInfo.nullable) {
                    return null;
                } else {
                    throw new IllegalArgumentException("Relation is null");
                }
            }

            return toEqualsPredicate(checkRelationMetaInfo.fromFkToPkFieldMapping, root, cb, object);
        }
    }

    private boolean mappingFieldsAreNull(Object object, Map<DatabaseFieldPath, DatabaseFieldPath> fromFkToPkFieldMapping) {
        for (DatabaseFieldPath key : fromFkToPkFieldMapping.keySet()) {
            if (key.getValueFromObject(object) != null) {
                return false;
            }
        }

        return true;
    }

    private record CheckRelationsMetaInfo(Set<CheckRelationMetaInfo> checkRelationMetaInfo) {

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            CheckRelationsMetaInfo that = (CheckRelationsMetaInfo) o;
            return Objects.equals(checkRelationMetaInfo, that.checkRelationMetaInfo);
        }

        @Override
        public int hashCode() {
            return Objects.hash(checkRelationMetaInfo);
        }
    }

    private record CheckRelationMetaInfo(DatabaseFieldPath relationField,
                                         Map<DatabaseFieldPath, DatabaseFieldPath> fromFkToPkFieldMapping,

                                         Set<DatabaseFieldPath> relationClassIds,
                                         Class<?> relationClass,
                                         String message,
                                         String relationErrorMessageNaming,

                                         boolean nullable) {

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            CheckRelationMetaInfo that = (CheckRelationMetaInfo) o;
            return Objects.equals(relationField, that.relationField) && Objects.equals(fromFkToPkFieldMapping, that.fromFkToPkFieldMapping) && Objects.equals(relationClass, that.relationClass);
        }

        @Override
        public int hashCode() {
            return Objects.hash(relationField, fromFkToPkFieldMapping, relationClass);
        }
    }
}
