package com.hv.meeting_scheduler_api_java.availability;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class AvailabilityEngine {

    private AvailabilityEngine() {}

    public static List<Interval> merge(List<Interval> intervals) {
        if (intervals.isEmpty()) {
            return List.of();
        }

        List<Interval> sorted = new ArrayList<>(intervals);
        sorted.sort(Comparator.comparingLong(Interval::start));

        List<Interval> merged = new ArrayList<>();
        long start = sorted.get(0).start();
        long end = sorted.get(0).end();

        for(Interval current: sorted) {
            if (end >= current.start()) {
                end = Math.max(end, current.end());
            } else {
                merged.add(new Interval(start, end));
                start = current.start();
                end = current.end();
            }
        }
        merged.add(new Interval(start, end));
        return merged;
    }

    public static List<Interval> subtract(Interval window, List<Interval> blocked) {
        List<Interval> freeGaps = new ArrayList<>();
        long cursor = window.start();

        for (Interval block: merge(blocked)) {
            long clipStart = Math.max(block.start(), window.start());
            long clipEnd = Math.min(block.end(), window.end());
            if (clipStart >= clipEnd) {
                continue;
            }
            if (clipStart > cursor) {
                freeGaps.add(new Interval(cursor, clipStart));
            }
            cursor = Math.max(cursor, clipEnd);
        }

        if (cursor < window.end()) {
            freeGaps.add(new Interval(cursor, window.end()));
        }

        return freeGaps;
    }

    public static List<Long> chunk(Interval gap, long duration) {
        if (duration <= 0) {
            throw new IllegalArgumentException("duration must be positive, got " + duration);
        }

        List<Long> starts = new ArrayList<>();
        for (long t = gap.start(); t + duration <= gap.end(); t += duration) {
            starts.add(t);
        }
        return starts;
    }

    public static List<Long> computeSlots(SlotQuery query) {
        EventConfig config = query.config();

        long start = Math.max(
                Math.max(query.window().start(), query.now() + config.minNotice()),
                query.queryStart());
        long end = Math.min(
                Math.min(query.window().end(), query.now() + config.maxDays()),
                query.queryEnd());
        if (start >= end) {
            return List.of();
        }

        List<Interval> blocked = query.bookings().stream()
                .map(b -> new Interval(b.start() - config.bufferBefore(), b.end() + config.bufferAfter()))
                .toList();

        List<Long> slots = new ArrayList<>();
        for (Interval gap : subtract(new Interval(start, end), blocked)) {
            slots.addAll(chunk(gap, config.duration()));
        }
        return slots;
    }

    public static List<Interval> resolveWindowsForDate(
            List<AvailabilityEntry> entries, LocalDate date, ZoneId zone) {

        int weekday = date.getDayOfWeek().getValue() % 7;

        List<AvailabilityEntry> overrides = entries.stream()
                .filter(e -> date.equals(e.specificDate()))
                .toList();

        List<AvailabilityEntry> matched = !overrides.isEmpty()
                ? overrides
                : entries.stream()
                .filter(e -> Objects.equals(e.dayOfWeek(), weekday))
                .toList();

        return matched.stream()
                .map(e -> new Interval(
                        localTimeToInstant(date, e.startTime(), zone),
                        localTimeToInstant(date, e.endTime(), zone)))
                .toList();
    }

    public static long localTimeToInstant(LocalDate date, LocalTime time, ZoneId zone) {
        return ZonedDateTime.of(date, time, zone).toInstant().toEpochMilli();
    }
}
