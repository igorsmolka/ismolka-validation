package com.ismolka.validation.validator;

import com.ismolka.validation.utils.metainfo.FieldMetaInfo;
import com.ismolka.validation.utils.metainfo.FieldPath;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.antlr.v4.runtime.misc.OrderedHashSet;
import org.springframework.orm.jpa.EntityManagerFactoryUtils;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Set;

public abstract class AbstractEntityManagerConstraintValidator<T extends Annotation, A> implements ConstraintValidator<T, A>, JpaTransactionManagerConstraintValidator {

//    @PersistenceContext
//    protected EntityManager em;

    protected JpaTransactionManager jpaTransactionManager;

    protected String message;

    @Override
    public boolean isValid(A value, ConstraintValidatorContext context) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(jpaTransactionManager, new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRED));
        EntityManagerFactory emf = jpaTransactionManager.getEntityManagerFactory();

        if (emf == null) {
            throw new RuntimeException("EntityManagerFactory is not provided");
        }

        return Boolean.TRUE.equals(transactionTemplate.execute(action -> {
            EntityManager em = EntityManagerFactoryUtils.getTransactionalEntityManager(emf);
            return isValid(value, context, em);
        }));
    }

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

    @Override
    public void setJpaTransactionManager(JpaTransactionManager jpaTransactionManager) {
        this.jpaTransactionManager = jpaTransactionManager;
    }
}
