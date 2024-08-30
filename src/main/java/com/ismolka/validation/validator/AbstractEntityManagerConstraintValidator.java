package com.ismolka.validation.validator;

import com.ismolka.validation.utils.metainfo.FieldMetaInfo;
import com.ismolka.validation.utils.metainfo.FieldPath;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.validation.ConstraintValidator;
import org.antlr.v4.runtime.misc.OrderedHashSet;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Set;

public abstract class AbstractEntityManagerConstraintValidator<T extends Annotation, A> implements ConstraintValidator<T, A> {

    @PersistenceContext
    protected EntityManager em;

    protected String message;

    protected Predicate toEqualsPredicate(Map<FieldPath, FieldPath> sourceTargetMap, Root<?> root, CriteriaBuilder cb, Object object) {
        Set<Predicate> keyPredicates = new OrderedHashSet<>();

        for (FieldPath sourcePath : sourceTargetMap.keySet()) {
            Object valueFromObject = sourcePath.getValueFromObject(object);

            FieldPath targetPath = sourceTargetMap.get(sourcePath);

            if (valueFromObject == null) {
                keyPredicates.add(cb.isNull(asPersistencePath(root, targetPath)));
                continue;
            }

            keyPredicates.add(cb.equal(asPersistencePath(root, targetPath), valueFromObject));
        }

        return cb.and(keyPredicates.toArray(Predicate[]::new));
    }


    protected Predicate toEqualsPredicate(Set<FieldPath> constraintKey, Root<?> root, CriteriaBuilder cb, Object object) {
        Set<Predicate> keyPredicates = new OrderedHashSet<>();

        for (FieldPath metaInfoFieldPath : constraintKey) {
            Object valueFromObject = metaInfoFieldPath.getValueFromObject(object);

            if (valueFromObject == null) {
                keyPredicates.add(cb.isNull(asPersistencePath(root, metaInfoFieldPath)));
                continue;
            }

            keyPredicates.add(cb.equal(asPersistencePath(root, metaInfoFieldPath), valueFromObject));
        }

        return cb.and(keyPredicates.toArray(Predicate[]::new));
    }

    protected Path<?> asPersistencePath(Root<?> root, FieldPath metaInfoField) {
        Path<?> path = root;

        for (FieldMetaInfo fieldMetaInfo : metaInfoField.pathFieldChain()) {
            path = path.get(fieldMetaInfo.name());
        }

        return path;
    }

    protected abstract void extractAndCashMetaDataForClass(Class<?> clazz);
}
