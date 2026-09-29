package com.hv.meeting_scheduler_api_java.dto;

import java.util.List;
import java.util.UUID;

public record AvailabilityResponse(
        UUID eventTypeId,
        String inviteeTimezone,
        List<String> slots
) {}