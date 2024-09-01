package com.ismolka.validation.validator;

import jakarta.persistence.EntityManager;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.orm.jpa.JpaTransactionManager;

/**
 * This interface needed for injecting a {@link JpaTransactionManager} and using an {@link EntityManager} in the validator.
 *
 * @author Ihar Smolka
 */
public interface JpaTransactionManagerConstraintValidator {

    /**
     * Validation method with using an {@link EntityManager}.
     *
     * @param value - value for validation
     * @param context - {@link ConstraintValidatorContext}
     * @param entityManager - {@link EntityManager}
     * @return validation result
     */
    boolean isValid(Object value, ConstraintValidatorContext context, EntityManager entityManager);

    /**
     * @param jpaTransactionManager - {@link JpaTransactionManager}
     */
    void setJpaTransactionManager(JpaTransactionManager jpaTransactionManager);
}
