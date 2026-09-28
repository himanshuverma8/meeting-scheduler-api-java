package com.hv.meeting_scheduler_api_java.availability;

import java.time.LocalDate;
import java.time.LocalTime;

public record AvailabilityEntry(
        Integer dayOfWeek,
        LocalDate specificDate,
        LocalTime startTime,
        LocalTime endTime
) {
}
