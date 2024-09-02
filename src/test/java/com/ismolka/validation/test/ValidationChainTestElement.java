package com.ismolka.validation.test;

import com.ismolka.validation.test.model.TestElement;
import com.ismolka.validation.validator.ValidationChainElement;
import jakarta.validation.ConstraintValidatorContext;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;
import org.springframework.stereotype.Service;

@Service
public class ValidationChainTestElement implements ValidationChainElement<TestElement> {

    @Override
    public boolean isValid(TestElement object, ConstraintValidatorContext context) {
        context.unwrap(HibernateConstraintValidatorContext.class)
                .buildConstraintViolationWithTemplate("TEST TEMPLATE")
                .addConstraintViolation();

        return false;
    }
}
