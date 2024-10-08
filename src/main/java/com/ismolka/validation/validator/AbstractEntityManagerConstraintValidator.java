package com.ismolka.validation.validator;

import com.ismolka.validation.utils.metainfo.DatabaseFieldMetaInfo;
import com.ismolka.validation.utils.metainfo.DatabaseFieldPath;
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

    protected Predicate toEqualsPredicate(Map<DatabaseFieldPath, DatabaseFieldPath> sourceTargetMap, Root<?> root, CriteriaBuilder cb, Object object) {
        Set<Predicate> keyPredicates = new OrderedHashSet<>();

        for (DatabaseFieldPath sourcePath : sourceTargetMap.keySet()) {
            Object valueFromObject = sourcePath.getValueFromObject(object);

            DatabaseFieldPath targetPath = sourceTargetMap.get(sourcePath);

            if (valueFromObject == null) {
                keyPredicates.add(cb.isNull(asPersistencePath(root, targetPath)));
                continue;
            }

            keyPredicates.add(cb.equal(asPersistencePath(root, targetPath), valueFromObject));
        }

        return cb.and(keyPredicates.toArray(Predicate[]::new));
    }


    protected Predicate toEqualsPredicate(Set<DatabaseFieldPath> constraintKey, Root<?> root, CriteriaBuilder cb, Object object) {
        Set<Predicate> keyPredicates = new OrderedHashSet<>();

        for (DatabaseFieldPath metaInfoFieldPath : constraintKey) {
            Object valueFromObject = metaInfoFieldPath.getValueFromObject(object);

            if (valueFromObject == null) {
                keyPredicates.add(cb.isNull(asPersistencePath(root, metaInfoFieldPath)));
                continue;
            }

            keyPredicates.add(cb.equal(asPersistencePath(root, metaInfoFieldPath), valueFromObject));
        }

        return cb.and(keyPredicates.toArray(Predicate[]::new));
    }

    protected Path<?> asPersistencePath(Root<?> root, DatabaseFieldPath databaseFieldPath) {
        Path<?> path = root;

        for (DatabaseFieldMetaInfo databaseFieldMetaInfo : databaseFieldPath.pathFieldChain()) {
            path = path.get(databaseFieldMetaInfo.field().name());
        }

        return path;
    }

    protected abstract void extractAndCashMetaDataForClass(Class<?> clazz);

    @Override
    public void setJpaTransactionManager(JpaTransactionManager jpaTransactionManager) {
        this.jpaTransactionManager = jpaTransactionManager;
    }
}
