package com.ismolka.validation.test;

import com.ismolka.validation.test.config.TestConfig;
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
public class ValidationChainTest {

    private static final int EXPECTED_CONSTRAINT_COUNT_ON_FAIL = 1;

    @Autowired
    private Validator validator;

    @Test
    public void test_onFail() {
        ChainTestObject chainTestObject = new ChainTestObject();

        Set<ConstraintViolation<ChainTestObject>> constraintViolationSet = validator.validate(chainTestObject);

        Assertions.assertEquals(EXPECTED_CONSTRAINT_COUNT_ON_FAIL, constraintViolationSet.size());
    }
}
