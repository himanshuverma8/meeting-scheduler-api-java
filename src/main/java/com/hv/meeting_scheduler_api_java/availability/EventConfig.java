package com.hv.meeting_scheduler_api_java.availability;

public record EventConfig(
        long duration,
        long bufferBefore,
        long bufferAfter,
        long minNotice,
        long maxDays
) {}
