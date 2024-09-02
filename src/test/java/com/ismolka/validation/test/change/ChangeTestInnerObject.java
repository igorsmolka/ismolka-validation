package com.ismolka.validation.test.change;

import java.util.Objects;

public class ChangeTestInnerObject {

    private String valueFromObject;

    public ChangeTestInnerObject(String valueFromObject) {
        this.valueFromObject = valueFromObject;
    }

    public String getValueFromObject() {
        return valueFromObject;
    }

    public void setValueFromObject(String valueFromObject) {
        this.valueFromObject = valueFromObject;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChangeTestInnerObject that = (ChangeTestInnerObject) o;
        return Objects.equals(valueFromObject, that.valueFromObject);
    }

    @Override
    public int hashCode() {
        return Objects.hash(valueFromObject);
    }

    @Override
    public String toString() {
        return "ChangeTestInnerObject{" +
                "valueFromObject='" + valueFromObject + '\'' +
                '}';
    }
}
