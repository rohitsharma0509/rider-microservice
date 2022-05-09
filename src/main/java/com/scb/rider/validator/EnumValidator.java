package com.scb.rider.validator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

//@Target(ElementType.FIELD)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = EnumValidatorImpl.class)
public @interface EnumValidator {
        String message() default "{com.scb.rider.validator.EnumValidator.message}";
        Class<?>[] groups() default {};
        Class<? extends Payload>[] payload() default {};
        Class<? extends Enum> targetClassType();
    }
