package com.hv.meeting_scheduler_api_java.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateScheduleRequest(
        @NotBlank String name,
        @NotBlank String timezone
) {}
