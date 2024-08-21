package com.ismolka.validation.constraints;

import com.ismolka.validation.constraints.inner.ConstraintKey;
import com.ismolka.validation.constraints.inner.UnmodifiableAttribute;
import com.ismolka.validation.constraints.inner.UnmodifiableCollection;
import com.ismolka.validation.validator.CheckExistingByConstraintAndUnmodifiableAttributesValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = CheckExistingByConstraintAndUnmodifiableAttributesValidator.class)
public @interface CheckExistingByConstraintAndUnmodifiableAttributes {

    String message() default "{com.ismolka.validation.constraints.ExistsByConstraint.message}";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };

    ConstraintKey constraintKey();

    UnmodifiableAttribute[] unmodifiableAttributes() default {};

    UnmodifiableCollection[] unmodifiableCollections() default {};

    boolean stopUnmodifiableCheckOnFirstMismatch() default false;

    boolean loadByConstraint() default false;

    String loadingByUsingNamedEntityGraph() default "";
}
