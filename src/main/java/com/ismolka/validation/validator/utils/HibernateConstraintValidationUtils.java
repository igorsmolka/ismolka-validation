package com.ismolka.validation.validator.utils;

import com.ismolka.validation.validator.metainfo.FieldPath;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;

import java.util.Set;
import java.util.stream.Collectors;

public class HibernateConstraintValidationUtils {

    public static void fieldNameBatchesConstraintViolationBuild(HibernateConstraintValidatorContext constraintValidatorContext,
                                                                  Set<FieldPath> constraintKey,
                                                                  Object val,
                                                                  String fieldKey,
                                                                  String valueKey,
                                                                  String message) {
        String fields = constraintKey.stream().map(FieldPath::path).collect(Collectors.joining(", "));
        String values = constraintKey.stream().map(fieldMetaInfo -> String.valueOf(fieldMetaInfo.getValueFromObject(val))).collect(Collectors.joining(", "));

        constraintValidatorContext.addMessageParameter(fieldKey, fields);
        constraintValidatorContext.addMessageParameter(valueKey, values);

        constraintValidatorContext.buildConstraintViolationWithTemplate(message)
                .addConstraintViolation();
    }
}
