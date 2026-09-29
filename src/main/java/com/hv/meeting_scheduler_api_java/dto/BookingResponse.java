package com.hv.meeting_scheduler_api_java.dto;

import java.time.Instant;
import java.util.UUID;

public record BookingResponse(
        UUID id,
        UUID eventTypeId,
        Instant startTime,
        Instant endTime,
        Integer duration,
        String inviteeName,
        String inviteeEmail,
        String inviteeTimezone,
        String joinUrl,
        String hostUrl,
        Instant createdAt
) {}