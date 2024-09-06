package com.ismolka.validation.validator;

import com.ismolka.validation.constraints.ValidationChain;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.antlr.v4.runtime.misc.OrderedHashSet;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ValidationChainValidator implements ConstraintValidator<ValidationChain, Object> {

    private static final Map<Class<?>, Set<ValidationChainElement<?>>> META_INFO = new ConcurrentHashMap<>();

    private static final String VALIDATION_CHAIN_CLASS_PARAM_NAME = "validationChainClass";

    private static final String VALIDATION_CHAIN_OBJECT_CLASS_PARAM_NAME = "validationChainObjectClass";

    private static final String VALIDATION_CHAIN_OBJECT_PARAM_NAME = "validationChainObject";

    private Class<? extends ValidationChainElement<?>>[] chainClasses;

    @Autowired
    private ApplicationContext applicationContext;

    private boolean ignoreMainMessage;

    private String message;

    @Override
    public void initialize(ValidationChain constraintAnnotation) {
        this.chainClasses = constraintAnnotation.value();
        this.message = constraintAnnotation.message();
        this.ignoreMainMessage = !constraintAnnotation.addMessageToViolations();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        Class<?> clazz = value.getClass();

        if (!META_INFO.containsKey(clazz)) {
            extractAndCashMetaDataForClass(clazz);
        }

        Set<ValidationChainElement<?>> chain = META_INFO.get(clazz);

        return passThroughTheChain(value, context, chain);
    }

    protected void extractAndCashMetaDataForClass(Class<?> clazz) {
        Set<ValidationChainElement<?>> validationChainElements = new OrderedHashSet<>();
        for (Class<? extends ValidationChainElement<?>> validationChainElementClass : chainClasses) {
            ValidationChainElement<?> bean;
            try {
                bean = applicationContext.getBean(validationChainElementClass);
            } catch (BeansException exc) {
                throw new IllegalArgumentException(String.format("Problem with getting a bean for the class %s", validationChainElementClass));
            }

            validationChainElements.add(bean);
        }

        META_INFO.put(clazz, validationChainElements);
    }

    @SuppressWarnings("unchecked")
    private boolean passThroughTheChain(Object value, ConstraintValidatorContext context, Set<ValidationChainElement<?>> chain) {
        HibernateConstraintValidatorContext constraintValidatorContext = context.unwrap(HibernateConstraintValidatorContext.class);

        for (ValidationChainElement validationChainElement : chain) {
            boolean result = validationChainElement.isValid(value, context);

            if (!result) {
                if (ignoreMainMessage) {
                    constraintValidatorContext.disableDefaultConstraintViolation();
                } else {
                    constraintValidatorContext.addMessageParameter(VALIDATION_CHAIN_CLASS_PARAM_NAME, validationChainElement.getClass().getName())
                            .addMessageParameter(VALIDATION_CHAIN_OBJECT_CLASS_PARAM_NAME, value.getClass().getName())
                            .addMessageParameter(VALIDATION_CHAIN_OBJECT_PARAM_NAME, value);

                    constraintValidatorContext.buildConstraintViolationWithTemplate(message).addConstraintViolation();
                }

                return false;
            }
        }

        return true;
    }
}
