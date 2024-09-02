package com.ismolka.validation.test.change;

import java.util.Objects;

public class ChangeTestObjectMap {

    private String valueFromMap;

    public ChangeTestObjectMap(String valueFromMap) {
        this.valueFromMap = valueFromMap;
    }

    public String getValueFromMap() {
        return valueFromMap;
    }

    public void setValueFromMap(String valueFromMap) {
        this.valueFromMap = valueFromMap;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChangeTestObjectMap that = (ChangeTestObjectMap) o;
        return Objects.equals(valueFromMap, that.valueFromMap);
    }

    @Override
    public int hashCode() {
        return Objects.hash(valueFromMap);
    }

    @Override
    public String toString() {
        return "ChangeTestObjectMap{" +
                "valueFromMap='" + valueFromMap + '\'' +
                '}';
    }
}
