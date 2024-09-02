package com.ismolka.validation.test.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.util.Objects;

@Entity
@Table(name = "test_unmodifiable_foreign")
public class TestUnmodifiableForeign {

    @Id
    private Long id;

    @ManyToOne
    @JoinColumn(name = "test_id", referencedColumnName = "id")
    private TestUnmodifiable testUnmodifiable;

    @Column(name = "unmodifiable")
    private String unmodifiable;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TestUnmodifiable getTestUnmodifiable() {
        return testUnmodifiable;
    }

    public void setTestUnmodifiable(TestUnmodifiable testUnmodifiable) {
        this.testUnmodifiable = testUnmodifiable;
    }

    public String getUnmodifiable() {
        return unmodifiable;
    }

    public void setUnmodifiable(String unmodifiable) {
        this.unmodifiable = unmodifiable;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TestUnmodifiableForeign that = (TestUnmodifiableForeign) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "TestUnmodifiableForeign{" +
                "id=" + id +
                ", unmodifiable='" + unmodifiable + '\'' +
                '}';
    }
}
