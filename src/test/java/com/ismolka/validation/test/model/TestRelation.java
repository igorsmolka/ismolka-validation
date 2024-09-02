package com.ismolka.validation.test.model;

import com.ismolka.validation.constraints.CheckRelationsExistsConstraints;
import com.ismolka.validation.constraints.inner.RelationCheckConstraint;
import com.ismolka.validation.constraints.inner.RelationCheckConstraintFieldMapping;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.util.Objects;

@Entity
@Table(name = "test_relation")
@CheckRelationsExistsConstraints({
        @RelationCheckConstraint(
                relationField = "firstRelation"
        ),
        @RelationCheckConstraint(
                relationMapping = @RelationCheckConstraintFieldMapping(fromForeignKeyField = "secondRelationId", toPrimaryKeyField = "id"),
                relationClass = SecondRelation.class
        )
}
)
public class TestRelation {

    @Id
    private Long id;

    @OneToOne
    @JoinColumn(name = "first_relation_id", referencedColumnName = "id")
    private FirstRelation firstRelation;

    @Column(name = "second_relation_id")
    private Long secondRelationId;

    @OneToOne
    @JoinColumn(name = "second_relation_id", referencedColumnName = "id", insertable = false, updatable = false)
    private SecondRelation secondRelation;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public FirstRelation getFirstRelation() {
        return firstRelation;
    }

    public void setFirstRelation(FirstRelation firstRelation) {
        this.firstRelation = firstRelation;
    }

    public Long getSecondRelationId() {
        return secondRelationId;
    }

    public void setSecondRelationId(Long secondRelationId) {
        this.secondRelationId = secondRelationId;
    }

    public SecondRelation getSecondRelation() {
        return secondRelation;
    }

    public void setSecondRelation(SecondRelation secondRelation) {
        this.secondRelation = secondRelation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TestRelation that = (TestRelation) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "TestRelation{" +
                "id=" + id +
                ", firstRelation=" + firstRelation +
                ", secondRelationId=" + secondRelationId +
                ", secondRelation=" + secondRelation +
                '}';
    }
}
