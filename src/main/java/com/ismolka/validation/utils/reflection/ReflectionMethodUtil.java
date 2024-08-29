package com.ismolka.validation.utils.reflection;

import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.Arrays;

public class ReflectionMethodUtil {

    public static boolean methodIsNotPresent(Method method, Class<?> clazz) {
        return !Arrays.asList(ReflectionUtils.getDeclaredMethods(clazz)).contains(method);
    }
}
