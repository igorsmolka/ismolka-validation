package com.ismolka.validation.test;

import com.ismolka.validation.test.config.TestConfig;
import com.ismolka.validation.test.model.TestUnmodifiable;
import com.ismolka.validation.test.model.TestUnmodifiableForeign;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Set;

@SpringBootTest(classes = TestConfig.class)
@EnableAutoConfiguration
public class ExistingAndUnmodifiableTest {

    private static final int EXPECTED_CONSTRAINT_COUNT_ON_EXISTING_FAIL = 1;

    private static final int EXPECTED_CONSTRAINT_COUNT_ON_EXISTING_SUCCESS = 0;

    private static final int EXPECTED_CONSTRAINT_COUNT_ON_UNMODIFIED_FAIL = 2;

    @Autowired
    private Validator validator;

    @Test
    public void test_existingOnFail() {
        TestUnmodifiable testUnmodifiable = new TestUnmodifiable();

        testUnmodifiable.setId(2L);
        testUnmodifiable.setUnmodifiable("unmodifiable");

        TestUnmodifiableForeign foreign = new TestUnmodifiableForeign();

        foreign.setId(1L);
        foreign.setUnmodifiable("unmodifiable");
        foreign.setTestUnmodifiable(testUnmodifiable);

        testUnmodifiable.setForeigns(List.of(foreign));

        Set<ConstraintViolation<TestUnmodifiable>> constraintValidatorSet = validator.validate(testUnmodifiable);

        Assertions.assertEquals(EXPECTED_CONSTRAINT_COUNT_ON_EXISTING_FAIL, constraintValidatorSet.size());
    }

    @Test
    public void test_existingOnSuccessAndFieldsAreNotModified() {
        TestUnmodifiable testUnmodifiable = new TestUnmodifiable();

        testUnmodifiable.setId(1L);
        testUnmodifiable.setUnmodifiable("unmodifiable");

        TestUnmodifiableForeign foreign = new TestUnmodifiableForeign();

        foreign.setId(1L);
        foreign.setUnmodifiable("unmodifiable");
        foreign.setTestUnmodifiable(testUnmodifiable);

        testUnmodifiable.setForeigns(List.of(foreign));

        Set<ConstraintViolation<TestUnmodifiable>> constraintValidatorSet = validator.validate(testUnmodifiable);

        Assertions.assertEquals(EXPECTED_CONSTRAINT_COUNT_ON_EXISTING_SUCCESS, constraintValidatorSet.size());
    }

    @Test
    public void test_existingOnSuccessAndFieldsAreModified() {
        TestUnmodifiable testUnmodifiable = new TestUnmodifiable();

        testUnmodifiable.setId(1L);
        testUnmodifiable.setUnmodifiable("modifiable");

        TestUnmodifiableForeign foreign = new TestUnmodifiableForeign();

        foreign.setId(1L);
        foreign.setUnmodifiable("modifiable");
        foreign.setTestUnmodifiable(testUnmodifiable);

        testUnmodifiable.setForeigns(List.of(foreign));

        Set<ConstraintViolation<TestUnmodifiable>> constraintValidatorSet = validator.validate(testUnmodifiable);

        Assertions.assertEquals(EXPECTED_CONSTRAINT_COUNT_ON_UNMODIFIED_FAIL, constraintValidatorSet.size());
    }
}
