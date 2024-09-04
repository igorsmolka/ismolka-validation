package com.ismolka.validation.test;

import com.ismolka.validation.test.config.TestConfig;
import com.ismolka.validation.test.model.FirstRelation;
import com.ismolka.validation.test.model.SecondRelation;
import com.ismolka.validation.test.model.TestRelation;
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
public class RelationValidationTest {

    private static final int EXPECTED_CONSTRAINT_COUNT_ON_FAIL = 2;

    private static final int EXPECTED_CONSTRAINT_COUNT_ON_SUCCESS = 0;

    @Autowired
    private Validator validator;

    @Test
    public void test_relationsAreNull() {
        TestRelation testRelation = new TestRelation();

        testRelation.setFirstRelation(null);
        testRelation.setSecondRelation(null);
        testRelation.setSecondRelationId(null);

        Set<ConstraintViolation<TestRelation>> constraintValidatorSet = validator.validate(testRelation);

        Assertions.assertEquals(EXPECTED_CONSTRAINT_COUNT_ON_SUCCESS, constraintValidatorSet.size());
    }

    @Test
    public void test_onFail() {
        TestRelation testRelation = new TestRelation();

        FirstRelation firstRelation = new FirstRelation();
        firstRelation.setId(2L);
        firstRelation.setValue("VAL1");

        SecondRelation secondRelation = new SecondRelation();
        secondRelation.setId(2L);
        secondRelation.setValue("VAL2");

        testRelation.setFirstRelation(firstRelation);
        testRelation.setSecondRelation(secondRelation);
        testRelation.setSecondRelationId(secondRelation.getId());

        Set<ConstraintViolation<TestRelation>> constraintValidatorSet = validator.validate(testRelation);

        Assertions.assertEquals(EXPECTED_CONSTRAINT_COUNT_ON_FAIL, constraintValidatorSet.size());
    }

    @Test
    public void test_onSuccess() {
        TestRelation testRelation = new TestRelation();

        FirstRelation firstRelation = new FirstRelation();
        firstRelation.setId(1L);
        firstRelation.setValue("test1");

        SecondRelation secondRelation = new SecondRelation();
        secondRelation.setId(1L);
        secondRelation.setValue("test2");

        testRelation.setFirstRelation(firstRelation);
        testRelation.setSecondRelation(secondRelation);
        testRelation.setSecondRelationId(secondRelation.getId());

        Set<ConstraintViolation<TestRelation>> constraintValidatorSet = validator.validate(testRelation);

        Assertions.assertEquals(EXPECTED_CONSTRAINT_COUNT_ON_SUCCESS, constraintValidatorSet.size());
    }
}
