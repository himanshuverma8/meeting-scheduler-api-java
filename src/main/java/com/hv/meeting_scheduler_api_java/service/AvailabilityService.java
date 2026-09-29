package com.hv.meeting_scheduler_api_java.service;

import com.hv.meeting_scheduler_api_java.availability.*;
import com.hv.meeting_scheduler_api_java.domain.Booking;
import com.hv.meeting_scheduler_api_java.domain.EventType;
import com.hv.meeting_scheduler_api_java.domain.Schedule;
import com.hv.meeting_scheduler_api_java.domain.ScheduleEntry;
import com.hv.meeting_scheduler_api_java.dto.AvailabilityResponse;
import com.hv.meeting_scheduler_api_java.exception.ResourceNotFoundException;
import com.hv.meeting_scheduler_api_java.repository.BookingRepository;
import com.hv.meeting_scheduler_api_java.repository.EventTypeRepository;
import com.hv.meeting_scheduler_api_java.repository.ScheduleEntryRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.*;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class AvailabilityService {

    private final EventTypeRepository eventTypeRepository;
    private final ScheduleEntryRepository scheduleEntryRepository;
    private final BookingRepository bookingRepository;

    public AvailabilityService(EventTypeRepository eventTypeRepository,
                               ScheduleEntryRepository scheduleEntryRepository,
                               BookingRepository bookingRepository) {
        this.eventTypeRepository = eventTypeRepository;
        this.scheduleEntryRepository = scheduleEntryRepository;
        this.bookingRepository = bookingRepository;
    }

    @Transactional(readOnly = true)
    public AvailabilityResponse getAvailableSlots(
            UUID eventTypeId, String startDateIso, String endDateIso, String inviteeTimezone) {

        EventType eventType = eventTypeRepository.findById(eventTypeId)
                .orElseThrow(() -> new ResourceNotFoundException("event type not found"));

        ZoneId inviteeZone = parseZone(inviteeTimezone);
        long queryStart = parseInstant(startDateIso);
        long queryEnd = parseInstant(endDateIso);
        if (queryStart >= queryEnd) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "startDate must be before endDate");
        }

        Schedule schedule = eventType.getSchedule();
        ZoneId hostZone = ZoneId.of(schedule.getTimezone());

        List<AvailabilityEntry> entries = scheduleEntryRepository.findByScheduleId(schedule.getId()).stream()
                .map(this::toAvailabilityEntry)
                .toList();

        List<Interval> bookedIntervals = bookingRepository.findByHostId(eventType.getUser().getId()).stream()
                .map(b -> new Interval(b.getStartTime().toEpochMilli(), b.getEndTime().toEpochMilli()))
                .toList();

        EventConfig config = new EventConfig(
                eventType.getDuration() * 60_000L,
                eventType.getBufferBefore() * 60_000L,
                eventType.getBufferAfter() * 60_000L,
                eventType.getMinNoticeMinutes() * 60_000L,
                eventType.getMaxDaysInAdvance() * 86_400_000L
        );

        long now = Instant.now().toEpochMilli();
        List<Long> allSlots = new ArrayList<>();

        ZonedDateTime cursor = Instant.ofEpochMilli(queryStart).atZone(hostZone).toLocalDate().atStartOfDay(hostZone);
        ZonedDateTime lastDay = Instant.ofEpochMilli(queryEnd).atZone(hostZone).toLocalDate().atStartOfDay(hostZone);

        while (!cursor.isAfter(lastDay)) {
            LocalDate date = cursor.toLocalDate();
            List<Interval> windows = AvailabilityEngine.resolveWindowsForDate(entries, date, hostZone);

            for (Interval window : windows) {
                SlotQuery query = new SlotQuery(now, window, bookedIntervals, config, queryStart, queryEnd);
                allSlots.addAll(AvailabilityEngine.computeSlots(query));
            }
            cursor = cursor.plusDays(1);
        }

        List<String> formatted = allSlots.stream()
                .map(ms -> Instant.ofEpochMilli(ms).atZone(inviteeZone).toOffsetDateTime().toString())
                .toList();

        return new AvailabilityResponse(eventTypeId, inviteeTimezone, formatted);
    }

    private AvailabilityEntry toAvailabilityEntry(ScheduleEntry entry) {
        return new AvailabilityEntry(
                entry.getDayOfWeek(), entry.getSpecificDate(), entry.getStartTime(), entry.getEndTime());
    }

    private ZoneId parseZone(String zone) {
        try {
            return ZoneId.of(zone);
        } catch (DateTimeException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid IANA timezone: " + zone);
        }
    }

    private long parseInstant(String iso) {
        try {
            return OffsetDateTime.parse(iso).toInstant().toEpochMilli();
        } catch (DateTimeParseException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid ISO-8601 date-time: " + iso);
        }
    }
}