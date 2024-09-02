package com.ismolka.validation.test.change;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ChangeTestObject {

    private String simpleField;

    private ChangeTestInnerObject innerObject;

    private List<ChangeTestObjectCollection> collection;

    private Map<String, ChangeTestObjectMap> map;

    private ChangeTestObjectCollection[] array;

    public ChangeTestObject() {
    }

    public String getSimpleField() {
        return simpleField;
    }

    public void setSimpleField(String simpleField) {
        this.simpleField = simpleField;
    }

    public ChangeTestInnerObject getInnerObject() {
        return innerObject;
    }

    public void setInnerObject(ChangeTestInnerObject innerObject) {
        this.innerObject = innerObject;
    }

    public List<ChangeTestObjectCollection> getCollection() {
        return collection;
    }

    public void setCollection(List<ChangeTestObjectCollection> collection) {
        this.collection = collection;
    }

    public Map<String, ChangeTestObjectMap> getMap() {
        return map;
    }

    public void setMap(Map<String, ChangeTestObjectMap> map) {
        this.map = map;
    }

    public ChangeTestObjectCollection[] getArray() {
        return array;
    }

    public void setArray(ChangeTestObjectCollection[] array) {
        this.array = array;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChangeTestObject that = (ChangeTestObject) o;
        return Objects.equals(simpleField, that.simpleField) && Objects.equals(innerObject, that.innerObject) && Objects.equals(collection, that.collection) && Objects.equals(map, that.map) && Arrays.equals(array, that.array);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(simpleField, innerObject, collection, map);
        result = 31 * result + Arrays.hashCode(array);
        return result;
    }

    @Override
    public String toString() {
        return "ChangeTestObject{" +
                "simpleField='" + simpleField + '\'' +
                ", innerObject=" + innerObject +
                ", collection=" + collection +
                ", map=" + map +
                ", array=" + Arrays.toString(array) +
                '}';
    }
}
