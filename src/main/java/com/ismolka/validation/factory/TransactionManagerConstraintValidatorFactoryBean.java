package com.ismolka.validation.factory;

import com.ismolka.validation.validator.JpaTransactionManagerConstraintValidator;
import jakarta.validation.ConstraintValidator;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.validation.beanvalidation.SpringConstraintValidatorFactory;

/**
 * This class extends {@link SpringConstraintValidatorFactory} and is needed to inject {@link JpaTransactionManager} into database checking validators,
 *
 * @author Ihar Smolka
 */
public class TransactionManagerConstraintValidatorFactoryBean extends SpringConstraintValidatorFactory {

    private JpaTransactionManager jpaTransactionManager;

    /**
     * Create a new TransactionManagerConstraintValidatorFactoryBean for the given BeanFactory.
     *
     * @param beanFactory the target BeanFactory
     */
    public TransactionManagerConstraintValidatorFactoryBean(AutowireCapableBeanFactory beanFactory) {
        super(beanFactory);
    }

    @Override
    public <T extends ConstraintValidator<?, ?>> T getInstance(Class<T> key) {
        ConstraintValidator<?, ?> constraintValidator = super.getInstance(key);

        if (JpaTransactionManagerConstraintValidator.class.isAssignableFrom(constraintValidator.getClass())) {
            JpaTransactionManagerConstraintValidator jpaTransactionManagerConstraintValidator = (JpaTransactionManagerConstraintValidator) constraintValidator;
            jpaTransactionManagerConstraintValidator.setJpaTransactionManager(jpaTransactionManager);
        }

        return (T) constraintValidator;
    }

    public void setJpaTransactionManager(JpaTransactionManager jpaTransactionManager) {
        this.jpaTransactionManager = jpaTransactionManager;
    }
}
