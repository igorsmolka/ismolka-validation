package com.ismolka.validation.utils.metainfo;

import org.antlr.v4.runtime.misc.OrderedHashSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ReflectionUtils;

import java.beans.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MetaInfoExtractorUtil {

    private static final Logger log = LoggerFactory.getLogger(MetaInfoExtractorUtil.class);

    public static Set<FieldPath> extractFieldPathsMetaInfo(String[] paths, Class<?> rootClass) {
        Set<FieldPath> resultSet = new OrderedHashSet<>();

        for (String path : paths) {
            resultSet.add(extractFieldPathMetaInfo(path, rootClass));
        }

        return resultSet;
    }

    public static FieldPath extractFieldPathMetaInfo(String path, Class<?> rootClass) {
        List<FieldMetaInfo> pathFieldChain = new ArrayList<>();

        String[] pathParts = path.split("\\.");

        Class<?> targetClass = rootClass;

        for (String pathPart : pathParts) {
            FieldMetaInfo extractedFieldMetaInfo = extractFieldMetaInfo(pathPart, targetClass);

            targetClass = extractedFieldMetaInfo.clazz();

            pathFieldChain.add(extractedFieldMetaInfo);
        }

        return new FieldPath(rootClass, path, pathFieldChain);
    }


    public static FieldMetaInfo extractFieldMetaInfo(String name, Class<?> clazz) {
        Method readMethod = null;
        try {
            PropertyDescriptor propertyDescriptor = new PropertyDescriptor(name, clazz);
            readMethod = propertyDescriptor.getReadMethod();
        } catch (IntrospectionException introspectionException) {
            log.warn("PropertyDescriptor error: {}", introspectionException.getMessage());
        }

        Field field = ReflectionUtils.findField(clazz, name);

        if (field == null) {
            throw new IllegalArgumentException(String.format("Field with name %s doesn't exist in class %s", name, clazz));
        }

        return new FieldMetaInfo(name,
                readMethod,
                field,
                field.getType(),
                clazz);
    }
}
