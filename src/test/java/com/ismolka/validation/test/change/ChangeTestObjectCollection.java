package com.ismolka.validation.test.change;

import java.util.Objects;

public class ChangeTestObjectCollection {

    private String key;

    private String valueFromCollection;

    public ChangeTestObjectCollection(String key, String valueFromCollection) {
        this.key = key;
        this.valueFromCollection = valueFromCollection;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValueFromCollection() {
        return valueFromCollection;
    }

    public void setValueFromCollection(String valueFromCollection) {
        this.valueFromCollection = valueFromCollection;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChangeTestObjectCollection that = (ChangeTestObjectCollection) o;
        return Objects.equals(key, that.key) && Objects.equals(valueFromCollection, that.valueFromCollection);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, valueFromCollection);
    }

    @Override
    public String toString() {
        return "ChangeTestObjectCollection{" +
                "key='" + key + '\'' +
                ", valueFromCollection='" + valueFromCollection + '\'' +
                '}';
    }
}
