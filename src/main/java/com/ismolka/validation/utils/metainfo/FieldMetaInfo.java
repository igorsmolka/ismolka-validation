package com.ismolka.validation.utils.metainfo;

import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Objects;

public record FieldMetaInfo(String name,
                            Method readMethod,
                            Field field,
                            boolean embeddedId,

                            boolean embeddable,
                            boolean simpleId,
                            Class<?> clazz,
                            Class<?> declaringClass,
                            boolean join
) {

    public Object getValueFromObject(Object obj) {
        if (obj == null) {
            return null;
        }

        if (readMethod != null) {
            ReflectionUtils.makeAccessible(readMethod);
            return ReflectionUtils.invokeMethod(readMethod, obj);
        }

        ReflectionUtils.makeAccessible(field);
        return ReflectionUtils.getField(field, obj);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FieldMetaInfo that = (FieldMetaInfo) o;
        return Objects.equals(name, that.name) && Objects.equals(declaringClass, that.declaringClass);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, declaringClass);
    }
}
