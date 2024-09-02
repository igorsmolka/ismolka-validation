package com.ismolka.validation.validator.utils;

import com.ismolka.validation.utils.metainfo.DatabaseFieldPath;
import com.ismolka.validation.utils.metainfo.FieldPath;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;

import java.util.Set;
import java.util.stream.Collectors;

public class HibernateConstraintValidationUtils {

    public static void fieldNameBatchesConstraintViolationBuild(HibernateConstraintValidatorContext constraintValidatorContext,
                                                                  Set<DatabaseFieldPath> constraintKey,
                                                                  Object val,
                                                                  String fieldKey,
                                                                  String valueKey,
                                                                  String message) {
        String fields = constraintKey.stream().map(DatabaseFieldPath::path).collect(Collectors.joining(", "));
        String values = constraintKey.stream().map(fieldMetaInfo -> String.valueOf(fieldMetaInfo.getValueFromObject(val))).collect(Collectors.joining(", "));

        constraintValidatorContext.addMessageParameter(fieldKey, fields);
        constraintValidatorContext.addMessageParameter(valueKey, values);

        constraintValidatorContext.buildConstraintViolationWithTemplate(message)
                .addConstraintViolation();
    }
}
