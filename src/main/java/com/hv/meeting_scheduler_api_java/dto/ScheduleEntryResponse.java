package com.hv.meeting_scheduler_api_java.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record ScheduleEntryResponse(
        UUID id,
        Integer dayOfWeek,
        LocalDate specificDate,
        LocalTime startTime,
        LocalTime endTime
) {}