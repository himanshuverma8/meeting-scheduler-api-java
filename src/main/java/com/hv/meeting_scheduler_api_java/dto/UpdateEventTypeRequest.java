package com.hv.meeting_scheduler_api_java.dto;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record UpdateEventTypeRequest(
        UUID scheduleId,
        @Size(min = 1) String name,
        @Positive Integer duration,
        @PositiveOrZero Integer bufferBefore,
        @PositiveOrZero Integer bufferAfter,
        @PositiveOrZero Integer minNoticeMinutes,
        @Positive Integer maxDaysInAdvance
) {}