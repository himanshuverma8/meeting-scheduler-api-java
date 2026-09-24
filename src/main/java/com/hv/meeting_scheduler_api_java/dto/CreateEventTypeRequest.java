package com.hv.meeting_scheduler_api_java.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.util.UUID;

public record CreateEventTypeRequest(
        @NotNull UUID scheduleId,
        @NotBlank String name,
        @PositiveOrZero Integer duration,
        @NotNull @PositiveOrZero Integer bufferBefore,
        @NotNull @PositiveOrZero Integer bufferAfter,
        @NotNull @PositiveOrZero Integer minNoticeMinutes,
        @NotNull @PositiveOrZero Integer maxDaysInAdvance
        ) {}
