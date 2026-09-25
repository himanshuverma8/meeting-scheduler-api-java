package com.hv.meeting_scheduler_api_java.dto;

import com.hv.meeting_scheduler_api_java.validation.ValidTimezone;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CreateScheduleRequest(
        @NotBlank String name,
        @NotBlank @ValidTimezone String timezone,
        @NotEmpty @Valid List<ScheduleEntryRequest> entries
) {}
