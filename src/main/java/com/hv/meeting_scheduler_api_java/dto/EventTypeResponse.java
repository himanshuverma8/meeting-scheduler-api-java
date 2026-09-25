package com.hv.meeting_scheduler_api_java.dto;

import java.time.Instant;
import java.util.UUID;

public record EventTypeResponse(
        UUID id,
        UUID scheduleId,
        String name,
        Integer duration,
        Integer bufferBefore,
        Integer bufferAfter,
        Integer minNoticeMinutes,
        Integer maxDaysInAdvance,
        Instant createdAt,
        Instant updatedAt
) {}
