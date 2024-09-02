package com.ismolka.validation.test;

import com.ismolka.validation.test.config.TestConfig;
import com.ismolka.validation.test.model.TestLimit;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Set;

@SpringBootTest(classes = TestConfig.class)
@EnableAutoConfiguration
public class LimitValidationTest {

    private static final int EXPECTED_CONSTRAINT_COUNT_ON_FAIL = 1;

    private static final int EXPECTED_CONSTRAINT_COUNT_ON_SUCCESS = 0;

    @Autowired
    private Validator validator;

    @Test
    public void test_onFail() {
        TestLimit testLimit = new TestLimit();

        testLimit.setLimitedField("only_two_values");

        Set<ConstraintViolation<TestLimit>> constraintValidatorSet = validator.validate(testLimit);

        Assertions.assertEquals(EXPECTED_CONSTRAINT_COUNT_ON_FAIL, constraintValidatorSet.size());
    }

    @Test
    public void test_onSuccess() {
        TestLimit testLimit = new TestLimit();

        testLimit.setLimitedField("unique");

        Set<ConstraintViolation<TestLimit>> constraintValidatorSet = validator.validate(testLimit);

        Assertions.assertEquals(EXPECTED_CONSTRAINT_COUNT_ON_SUCCESS, constraintValidatorSet.size());
    }
}
