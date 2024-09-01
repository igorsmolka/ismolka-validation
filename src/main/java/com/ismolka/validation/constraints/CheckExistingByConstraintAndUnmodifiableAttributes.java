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

/**
 * Describes a check for existing by constraint (makes sense to provide PK) and for forbidden changes.
 *
 * @author Ihar Smolka
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = CheckExistingByConstraintAndUnmodifiableAttributesValidator.class)
public @interface CheckExistingByConstraintAndUnmodifiableAttributes {

    String message() default "{com.ismolka.validation.constraints.ExistsByConstraint.message}";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };

    /**
     * @return constraint for existing check.
     */
    ConstraintKey constraintKey();

    /**
     * @return unmodifiable attributes.
     */
    UnmodifiableAttribute[] unmodifiableAttributes() default {};

    /**
     * @return unmodifiable collections.
     */
    UnmodifiableCollection[] unmodifiableCollections() default {};

    /**
     * @return flag to stop checking on the first change (makes sense to provide 'false', when we are in need to have full information about forbidden changes).
     */
    boolean stopUnmodifiableCheckOnFirstMismatch() default false;

    /**
     * @return flag to load entity instance in validator (necessary, when we check for forbidden changes).
     */
    boolean loadByConstraint() default false;

    /**
     * @return which {@link jakarta.persistence.NamedEntityGraph} should be used for loading.
     */
    String loadingByUsingNamedEntityGraph() default "";
}
