package com.hv.meeting_scheduler_api_java.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ScheduleResponse(
        UUID id,
        String name,
        String timezone,
        Instant createdAt,
        Instant updatedAt,
        List<ScheduleEntryResponse> entries
) {}
