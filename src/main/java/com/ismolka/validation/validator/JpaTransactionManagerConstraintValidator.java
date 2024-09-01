package com.ismolka.validation.validator;

import jakarta.persistence.EntityManager;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.orm.jpa.JpaTransactionManager;

public interface JpaTransactionManagerConstraintValidator {

    boolean isValid(Object value, ConstraintValidatorContext context, EntityManager entityManager);

    void setJpaTransactionManager(JpaTransactionManager jpaTransactionManager);
}
