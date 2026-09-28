package com.hv.meeting_scheduler_api_java.availability;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AvailabilityEngineTest {

    private static final ZoneId NEW_YORK = ZoneId.of("America/New_York");

    private static long utc(String iso) {
        return Instant.parse(iso).toEpochMilli();
    }

    // ---------- merge ----------

    @Test
    void mergeKeepsNonOverlappingUnsortedIntervalsSeparate() {
        List<Interval> result = AvailabilityEngine.merge(List.of(
                new Interval(1, 2), new Interval(5, 6), new Interval(3, 4)));

        assertThat(result).containsExactly(
                new Interval(1, 2), new Interval(3, 4), new Interval(5, 6));
    }

    @Test
    void mergeCombinesOverlappingAndTouchingIntervals() {
        List<Interval> result = AvailabilityEngine.merge(List.of(
                new Interval(1, 5), new Interval(4, 8), new Interval(8, 10)));

        assertThat(result).containsExactly(new Interval(1, 10));
    }

    @Test
    void mergeOfEmptyListIsEmpty() {
        assertThat(AvailabilityEngine.merge(List.of())).isEmpty();
    }

    // ---------- subtract ----------

    @Test
    void subtractSplitsWindowAroundMiddleBlock() {
        List<Interval> result = AvailabilityEngine.subtract(
                new Interval(750, 960), List.of(new Interval(770, 820)));

        assertThat(result).containsExactly(new Interval(750, 770), new Interval(820, 960));
    }

    @Test
    void subtractIgnoresBlockBeforeWindow() {
        List<Interval> result = AvailabilityEngine.subtract(
                new Interval(750, 960), List.of(new Interval(600, 700)));

        assertThat(result).containsExactly(new Interval(750, 960));
    }

    @Test
    void subtractIgnoresBlockAfterWindow() {
        List<Interval> result = AvailabilityEngine.subtract(
                new Interval(750, 960), List.of(new Interval(980, 1000)));

        assertThat(result).containsExactly(new Interval(750, 960));
    }

    @Test
    void subtractClipsBlockStartingBeforeWindow() {
        List<Interval> result = AvailabilityEngine.subtract(
                new Interval(750, 960), List.of(new Interval(740, 800)));

        assertThat(result).containsExactly(new Interval(800, 960));
    }

    @Test
    void subtractOfFullyCoveredWindowIsEmpty() {
        List<Interval> result = AvailabilityEngine.subtract(
                new Interval(750, 960), List.of(new Interval(700, 1000)));

        assertThat(result).isEmpty();
    }

    // ---------- chunk ----------

    @Test
    void chunkSlicesGapIntoDurationSizedSlots() {
        assertThat(AvailabilityEngine.chunk(new Interval(820, 960), 30))
                .containsExactly(820L, 850L, 880L, 910L);
    }

    @Test
    void chunkReturnsNothingWhenGapSmallerThanDuration() {
        assertThat(AvailabilityEngine.chunk(new Interval(750, 770), 30)).isEmpty();
    }

    @Test
    void chunkKeepsSlotEndingExactlyAtGapEnd() {
        assertThat(AvailabilityEngine.chunk(new Interval(930, 960), 30))
                .containsExactly(930L);
    }

    @Test
    void chunkRejectsNonPositiveDuration() {
        assertThatThrownBy(() -> AvailabilityEngine.chunk(new Interval(0, 100), 0))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // ---------- computeSlots ----------

    @Test
    void computeSlotsFullScenarioEndToEnd() {
        SlotQuery query = new SlotQuery(
                720,
                new Interval(660, 960),
                List.of(new Interval(780, 810)),
                new EventConfig(30, 10, 10, 30, 999_999),
                660,
                960);

        assertThat(AvailabilityEngine.computeSlots(query))
                .containsExactly(820L, 850L, 880L, 910L);
    }

    @Test
    void computeSlotsIsEmptyWhenMinNoticePushesStartPastWindowEnd() {
        SlotQuery query = new SlotQuery(
                900,
                new Interval(660, 960),
                List.of(),
                new EventConfig(30, 0, 0, 100, 999_999),
                660,
                960);

        assertThat(AvailabilityEngine.computeSlots(query)).isEmpty();
    }

    @Test
    void computeSlotsRespectsMaxDaysHorizon() {
        SlotQuery query = new SlotQuery(
                0,
                new Interval(0, 1000),
                List.of(),
                new EventConfig(100, 0, 0, 0, 300),
                0,
                1000);

        assertThat(AvailabilityEngine.computeSlots(query))
                .containsExactly(0L, 100L, 200L);
    }

    // ---------- DST ----------

    @Test
    void sameLocalTimeMapsToDifferentUtcInstantsAcrossDst() {
        long winter = AvailabilityEngine.localTimeToInstant(
                LocalDate.of(2026, 1, 15), LocalTime.of(9, 0), NEW_YORK);
        long summer = AvailabilityEngine.localTimeToInstant(
                LocalDate.of(2026, 7, 15), LocalTime.of(9, 0), NEW_YORK);

        assertThat(winter).isEqualTo(utc("2026-01-15T14:00:00Z"));
        assertThat(summer).isEqualTo(utc("2026-07-15T13:00:00Z"));
    }

    @Test
    void springForwardGapTimeIsShiftedForward() {
        long instant = AvailabilityEngine.localTimeToInstant(
                LocalDate.of(2026, 3, 8), LocalTime.of(2, 30), NEW_YORK);

        assertThat(instant).isEqualTo(utc("2026-03-08T07:30:00Z"));
    }

    @Test
    void fallBackOverlapPicksEarlierOffset() {
        long instant = AvailabilityEngine.localTimeToInstant(
                LocalDate.of(2026, 11, 1), LocalTime.of(1, 30), NEW_YORK);

        assertThat(instant).isEqualTo(utc("2026-11-01T05:30:00Z"));
    }

    @Test
    void midnightToEightOnSpringForwardDayIsOnlySevenRealHours() {
        List<AvailabilityEntry> entries = List.of(
                new AvailabilityEntry(null, LocalDate.of(2026, 3, 8),
                        LocalTime.of(0, 0), LocalTime.of(8, 0)));

        List<Interval> windows = AvailabilityEngine.resolveWindowsForDate(
                entries, LocalDate.of(2026, 3, 8), NEW_YORK);

        assertThat(windows).hasSize(1);
        long hourMs = 3_600_000L;
        assertThat(windows.get(0).end() - windows.get(0).start()).isEqualTo(7 * hourMs);

        List<Long> hourlySlots = AvailabilityEngine.chunk(windows.get(0), hourMs);
        assertThat(hourlySlots).hasSize(7);
    }

    // ---------- resolveWindowsForDate ----------

    @Test
    void resolvesRecurringDayIntoInstantWindows() {
        List<AvailabilityEntry> entries = List.of(
                new AvailabilityEntry(4, null, LocalTime.of(9, 0), LocalTime.of(17, 0)));

        List<Interval> windows = AvailabilityEngine.resolveWindowsForDate(
                entries, LocalDate.of(2026, 7, 16), NEW_YORK);

        assertThat(windows).containsExactly(new Interval(
                utc("2026-07-16T13:00:00Z"), utc("2026-07-16T21:00:00Z")));
    }

    @Test
    void sundayIsDayZero() {
        List<AvailabilityEntry> entries = List.of(
                new AvailabilityEntry(0, null, LocalTime.of(10, 0), LocalTime.of(12, 0)));

        assertThat(AvailabilityEngine.resolveWindowsForDate(
                entries, LocalDate.of(2026, 7, 19), NEW_YORK)).hasSize(1);
        assertThat(AvailabilityEngine.resolveWindowsForDate(
                entries, LocalDate.of(2026, 7, 20), NEW_YORK)).isEmpty();
    }

    @Test
    void overrideReplacesRecurringRuleForThatDate() {
        List<AvailabilityEntry> entries = List.of(
                new AvailabilityEntry(1, null, LocalTime.of(9, 0), LocalTime.of(12, 0)),
                new AvailabilityEntry(null, LocalDate.of(2026, 7, 20),
                        LocalTime.of(13, 0), LocalTime.of(18, 0)));

        List<Interval> windows = AvailabilityEngine.resolveWindowsForDate(
                entries, LocalDate.of(2026, 7, 20), NEW_YORK);

        assertThat(windows).containsExactly(new Interval(
                utc("2026-07-20T17:00:00Z"), utc("2026-07-20T22:00:00Z")));
    }

    @Test
    void multipleRecurringEntriesOnOneDayGiveMultipleWindows() {
        List<AvailabilityEntry> entries = List.of(
                new AvailabilityEntry(4, null, LocalTime.of(9, 0), LocalTime.of(12, 0)),
                new AvailabilityEntry(4, null, LocalTime.of(13, 0), LocalTime.of(17, 0)));

        assertThat(AvailabilityEngine.resolveWindowsForDate(
                entries, LocalDate.of(2026, 7, 16), NEW_YORK)).hasSize(2);
    }

    // ---------- Interval ----------

    @Test
    void intervalRejectsEndBeforeStart() {
        assertThatThrownBy(() -> new Interval(10, 5))
                .isInstanceOf(IllegalArgumentException.class);
    }
}