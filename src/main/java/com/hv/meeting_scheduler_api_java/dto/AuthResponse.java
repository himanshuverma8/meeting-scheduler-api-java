package com.hv.meeting_scheduler_api_java.dto;

import java.util.UUID;

public record AuthResponse(
        UUID userId,
        String name,
        String email,
        String token
) {}
