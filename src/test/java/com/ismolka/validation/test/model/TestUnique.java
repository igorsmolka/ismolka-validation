package com.ismolka.validation.test.model;

import com.ismolka.validation.constraints.CheckUniqueAnnotationWithIgnoreMatchById;
import com.ismolka.validation.constraints.UniqueValidationConstraints;
import com.ismolka.validation.constraints.inner.ConstraintKey;
import com.ismolka.validation.test.ValidationGroups;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.util.Objects;

@Entity
@Table(name = "test_unique")
@CheckUniqueAnnotationWithIgnoreMatchById(groups = ValidationGroups.ForIgnoreOneMatchById.class)
@UniqueValidationConstraints(constraintKeys = {
        @ConstraintKey("uniqueField")
}
)
public class TestUnique {

    @Id
    private Long id;

    @Column(name = "unique_field")
    private String uniqueField;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUniqueField() {
        return uniqueField;
    }

    public void setUniqueField(String uniqueField) {
        this.uniqueField = uniqueField;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TestUnique that = (TestUnique) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "TestUnique{" +
                "id=" + id +
                ", uniqueField='" + uniqueField + '\'' +
                '}';
    }
}
