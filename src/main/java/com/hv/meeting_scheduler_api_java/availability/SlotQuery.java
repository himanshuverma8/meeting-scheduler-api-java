package com.hv.meeting_scheduler_api_java.availability;

import java.util.List;

public record SlotQuery(
        long now,
        Interval window,
        List<Interval> bookings,
        EventConfig config,
        long queryStart,
        long queryEnd
) {}
