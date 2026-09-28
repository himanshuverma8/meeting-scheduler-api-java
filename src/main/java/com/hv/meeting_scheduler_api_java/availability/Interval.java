package com.hv.meeting_scheduler_api_java.availability;

public record Interval(long start, long end) {
    public Interval {
        if (end < start) {
            throw new IllegalArgumentException("interval end " + end + " is before start " + start);
        }
    }
}
