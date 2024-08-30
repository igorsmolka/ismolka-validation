package com.ismolka.validation.utils.reflection;

import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;

public class ReflectUtil {

    public static boolean fieldPathIsNotPresent(String fieldPath, Class<?> clazz) {
        String[] pathParts = fieldPath.split("\\.");

        Class<?> targetClass = clazz;

        for (String fieldName : pathParts) {
            Field field = ReflectionUtils.findField(targetClass, fieldName);
            if (field == null) {
                return true;
            }

            targetClass = field.getType();
        }

        return false;
    }

    public static boolean methodIsNotPresent(Method method, Class<?> clazz) {
        return !Arrays.asList(ReflectionUtils.getDeclaredMethods(clazz)).contains(method);
    }
}
