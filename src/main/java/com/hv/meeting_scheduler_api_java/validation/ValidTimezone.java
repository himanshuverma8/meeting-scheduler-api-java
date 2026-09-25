package com.hv.meeting_scheduler_api_java.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = TimezoneValidator.class)
public @interface ValidTimezone {
    String message() default "must be a valid IANA timezone, e.g. Asia/Kolkata";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}