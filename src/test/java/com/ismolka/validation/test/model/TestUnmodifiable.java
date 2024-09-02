package com.ismolka.validation.test.model;

import com.ismolka.validation.constraints.CheckExistingByConstraintAndUnmodifiableAttributes;
import com.ismolka.validation.constraints.inner.ConstraintKey;
import com.ismolka.validation.constraints.inner.UnmodifiableAttribute;
import com.ismolka.validation.constraints.inner.UnmodifiableCollection;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.NamedAttributeNode;
import jakarta.persistence.NamedEntityGraph;
import jakarta.persistence.NamedSubgraph;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "test_unmodifiable")
@NamedEntityGraph(name = "unmodifiable.test.eg", attributeNodes = @NamedAttributeNode(value = "foreigns", subgraph = "unmodifiable.foreign.test.eq"), subgraphs = {
        @NamedSubgraph(name = "unmodifiable.foreign.test.eq", attributeNodes = {
                @NamedAttributeNode("testUnmodifiable")
        })
})
@CheckExistingByConstraintAndUnmodifiableAttributes(
        constraintKey = @ConstraintKey("id"),
        loadByConstraint = true,
        loadingByUsingNamedEntityGraph = "unmodifiable.test.eg",
        unmodifiableAttributes = {
                @UnmodifiableAttribute("unmodifiable")
        },
        unmodifiableCollections = {
                @UnmodifiableCollection(
                        value = "foreigns",
                        fieldsForMatching = "id",
                        equalsFields = "unmodifiable",
                        collectionGenericClass = TestUnmodifiableForeign.class
                )
        }
)
public class TestUnmodifiable {

    @Id
    private Long id;

    @Column(name = "unmodifiable")
    private String unmodifiable;

    @OneToMany(mappedBy = "testUnmodifiable")
    private List<TestUnmodifiableForeign> foreigns = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUnmodifiable() {
        return unmodifiable;
    }

    public void setUnmodifiable(String unmodifiable) {
        this.unmodifiable = unmodifiable;
    }

    public List<TestUnmodifiableForeign> getForeigns() {
        return foreigns;
    }

    public void setForeigns(List<TestUnmodifiableForeign> foreigns) {
        this.foreigns = foreigns;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TestUnmodifiable that = (TestUnmodifiable) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "TestUnmodifiable{" +
                "id=" + id +
                ", unmodifiable='" + unmodifiable + '\'' +
                ", foreigns=" + foreigns +
                '}';
    }
}
