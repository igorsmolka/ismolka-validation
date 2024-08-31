package com.ismolka.validation.utils.metainfo;

import jakarta.persistence.*;
import org.antlr.v4.runtime.misc.OrderedHashSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ReflectionUtils;

import java.beans.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class MetaInfoExtractorUtil {

    private static final Logger log = LoggerFactory.getLogger(MetaInfoExtractorUtil.class);

    public static Set<FieldPath> extractIdFieldPathsMetaInfo(Class<?> rootClass) {
        Set<FieldPath> resultPaths = new OrderedHashSet<>();

        String[] paths = Arrays.stream(rootClass.getDeclaredFields()).map(Field::getName).toArray(String[]::new);

        Arrays.stream(paths).forEach(path -> {
            FieldPath fieldPath = extractFieldPathMetaInfo(path, rootClass);

            if (fieldPath.isIdentifierPath()) {
                resultPaths.add(fieldPath);
            }
        });

        return resultPaths;
    }

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


    private static FieldMetaInfo extractFieldMetaInfo(String name, Class<?> clazz) {
        Method readMethod = null;
        try {
            PropertyDescriptor propertyDescriptor = new PropertyDescriptor(name, clazz);
            readMethod = propertyDescriptor.getReadMethod();
        } catch (IntrospectionException introspectionException) {
            log.warn("PropertyDescriptor error: {}", introspectionException.getMessage());
        }

        Field field = ReflectionUtils.findField(clazz, name);

        boolean simpleId = false;
        boolean embeddedId = false;
        boolean join = false;
        boolean embeddable = false;

        if (field == null) {
            throw new IllegalArgumentException(String.format("Field with name %s doesn't exist in class %s", name, clazz));
        }

        if (field.isAnnotationPresent(Id.class)) {
            simpleId = true;
        }


        if (field.isAnnotationPresent(EmbeddedId.class)) {
            embeddedId = true;
            simpleId = false;
        }

        if (field.isAnnotationPresent(JoinColumn.class) || field.isAnnotationPresent(JoinColumns.class) || field.isAnnotationPresent(JoinTable.class)) {
            join = true;
        }

        if (field.getType().isAnnotationPresent(Embeddable.class)) {
            embeddable = true;
        }

        return new FieldMetaInfo(name,
                readMethod,
                field,
                embeddedId,
                embeddable,
                simpleId,
                field.getType(),
                clazz,
                join);
    }
}
