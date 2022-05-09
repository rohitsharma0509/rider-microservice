package com.scb.rider.validator;

import org.apache.commons.lang3.StringUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

public class EnumValidatorImpl implements ConstraintValidator< EnumValidator , String > {
     private Set<String> allowedValues;

     @SuppressWarnings({
         "unchecked",
         "rawtypes"
     })
    @Override
    public void initialize(EnumValidator targetEnum) {
    Class < ? extends Enum > enumSelected = targetEnum.targetClassType();
         allowedValues = (Set < String > ) EnumSet.allOf(enumSelected).stream().map(e -> ((Enum< ? extends Enum < ? >> ) e).name())
        .collect(Collectors.toSet());
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return value == null || (StringUtils.isNotBlank(value) && allowedValues.contains(value));
    }
}