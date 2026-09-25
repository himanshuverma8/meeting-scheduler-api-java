package com.hv.meeting_scheduler_api_java.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.ZoneId;
import java.util.Set;

public class TimezoneValidator implements ConstraintValidator<ValidTimezone, String> {

    private static final Set<String> ZONES = ZoneId.getAvailableZoneIds();

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return value == null || ZONES.contains(value);
    }
}