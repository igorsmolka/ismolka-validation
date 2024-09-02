package com.ismolka.validation.test.model;

import com.ismolka.validation.constraints.LimitValidationConstraints;
import com.ismolka.validation.constraints.inner.ConstraintKey;
import com.ismolka.validation.constraints.inner.LimitValidationConstraintGroup;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.Objects;

@Entity
@Table(name = "test_limit")
@LimitValidationConstraints(
        limitValueConstraints = {
                @LimitValidationConstraintGroup(
                        constraintKeys = @ConstraintKey("limitedField"),
                        limit = 2
                )
        }
)
public class TestLimit {

    @Id
    private Long id;

    @Column(name = "limited_field")
    private String limitedField;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLimitedField() {
        return limitedField;
    }

    public void setLimitedField(String limitedField) {
        this.limitedField = limitedField;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TestLimit testLimit = (TestLimit) o;
        return Objects.equals(id, testLimit.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "TestLimit{" +
                "id=" + id +
                ", limitedField='" + limitedField + '\'' +
                '}';
    }
}
