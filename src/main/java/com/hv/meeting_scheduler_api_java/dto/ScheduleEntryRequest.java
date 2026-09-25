package com.hv.meeting_scheduler_api_java.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

public record ScheduleEntryRequest(
        @Min(0) @Max(6) Integer dayOfWeek,
        LocalDate specificDate,
        @NotNull LocalTime startTime,
        @NotNull LocalTime endTime
) {

    @AssertTrue(message = "exactly one of dayOfWeek or specificDate is required")
    public boolean isDayOrDateExclusive() {
        return (dayOfWeek != null) ^ (specificDate != null);
    }

    @AssertTrue(message = "endTime must be after startTime")
    public boolean isEndAfterStart() {
        return startTime == null || endTime == null || endTime.isAfter(startTime);
    }
}