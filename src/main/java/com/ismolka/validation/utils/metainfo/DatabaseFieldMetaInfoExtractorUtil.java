package com.ismolka.validation.utils.metainfo;

import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.JoinTable;
import org.antlr.v4.runtime.misc.OrderedHashSet;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class DatabaseFieldMetaInfoExtractorUtil {

    public static Set<DatabaseFieldPath> extractIdFieldPathsMetaInfo(Class<?> rootClass) {
        Set<DatabaseFieldPath> resultPaths = new OrderedHashSet<>();

        String[] paths = Arrays.stream(rootClass.getDeclaredFields()).map(Field::getName).toArray(String[]::new);

        Arrays.stream(paths).forEach(path -> {
            DatabaseFieldPath fieldPath = extractDatabaseFieldPathMetaInfo(path, rootClass);

            if (fieldPath.isIdentifierPath()) {
                resultPaths.add(fieldPath);
            }
        });

        return resultPaths;
    }


    public static DatabaseFieldPath extractDatabaseFieldPathMetaInfo(String path, Class<?> rootClass) {
        List<DatabaseFieldMetaInfo> pathFieldChain = new ArrayList<>();

        String[] pathParts = path.split("\\.");

        Class<?> targetClass = rootClass;

        for (String pathPart : pathParts) {
            DatabaseFieldMetaInfo extractedFieldMetaInfo = extractDatabaseFieldMetaInfo(pathPart, targetClass);

            targetClass = extractedFieldMetaInfo.field().clazz();

            pathFieldChain.add(extractedFieldMetaInfo);
        }

        return new DatabaseFieldPath(rootClass, path, pathFieldChain);
    }

    public static DatabaseFieldMetaInfo extractDatabaseFieldMetaInfo(String name, Class<?> clazz) {
        FieldMetaInfo fieldMetaInfo = MetaInfoExtractorUtil.extractFieldMetaInfo(name, clazz);
        Field field = fieldMetaInfo.field();

        boolean simpleId = false;
        boolean embeddedId = false;
        boolean join = false;
        boolean embeddable = false;

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

        return new DatabaseFieldMetaInfo(
                fieldMetaInfo,
                embeddedId,
                embeddable,
                simpleId,
                join
        );
    }
}
