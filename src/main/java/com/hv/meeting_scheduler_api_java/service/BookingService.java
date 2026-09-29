package com.hv.meeting_scheduler_api_java.service;

import com.hv.meeting_scheduler_api_java.domain.Booking;
import com.hv.meeting_scheduler_api_java.domain.EventType;
import com.hv.meeting_scheduler_api_java.dto.BookingResponse;
import com.hv.meeting_scheduler_api_java.dto.CreateBookingRequest;
import com.hv.meeting_scheduler_api_java.exception.ResourceNotFoundException;
import com.hv.meeting_scheduler_api_java.exception.SlotAlreadyBookedException;
import com.hv.meeting_scheduler_api_java.repository.BookingRepository;
import com.hv.meeting_scheduler_api_java.repository.EventTypeRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.sql.SQLException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Service
public class BookingService {

    private static final String EXCLUSION_VIOLATION_SQLSTATE = "23P01";

    private final EventTypeRepository eventTypeRepository;
    private final BookingRepository bookingRepository;

    public BookingService(EventTypeRepository eventTypeRepository, BookingRepository bookingRepository) {
        this.eventTypeRepository = eventTypeRepository;
        this.bookingRepository = bookingRepository;
    }

    @Transactional
    public BookingResponse createBooking(CreateBookingRequest request, String idempotencyKey) {
        String key = normalize(idempotencyKey);

        if (key != null) {
            Optional<Booking> existing = bookingRepository.findByIdempotencyKey(key);
            if (existing.isPresent()) {
                return toResponse(existing.get());
            }
        }

        EventType eventType = eventTypeRepository.findById(request.eventTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("event type not found"));

        Instant startTime = parseInstant(request.startTime());
        Instant endTime = startTime.plus(eventType.getDuration(), ChronoUnit.MINUTES);

        Booking booking = new Booking(
                eventType.getUser(), eventType, key, startTime, endTime, eventType.getDuration(),
                request.inviteeName(), request.inviteeEmail(), request.inviteeTimezone(), null, null);

        Booking saved;
        try {
            saved = bookingRepository.saveAndFlush(booking);
        } catch (DataIntegrityViolationException ex) {
            if (key != null) {
                Optional<Booking> winner = bookingRepository.findByIdempotencyKey(key);
                if (winner.isPresent()) {
                    return toResponse(winner.get());
                }
            }
            if (isExclusionViolation(ex)) {
                throw new SlotAlreadyBookedException("this slot was just booked");
            }
            throw ex;
        }

        return toResponse(saved);
    }

    private String normalize(String idempotencyKey) {
        return (idempotencyKey == null || idempotencyKey.isBlank()) ? null : idempotencyKey;
    }

    private boolean isExclusionViolation(DataIntegrityViolationException ex) {
        Throwable root = ex.getMostSpecificCause();
        return root instanceof SQLException sqlEx
                && EXCLUSION_VIOLATION_SQLSTATE.equals(sqlEx.getSQLState());
    }

    private Instant parseInstant(String iso) {
        try {
            return OffsetDateTime.parse(iso).toInstant();
        } catch (DateTimeParseException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid ISO-8601 date-time: " + iso);
        }
    }

    private BookingResponse toResponse(Booking booking) {
        return new BookingResponse(
                booking.getId(), booking.getEventType().getId(), booking.getStartTime(), booking.getEndTime(),
                booking.getDuration(), booking.getInviteeName(), booking.getInviteeEmail(),
                booking.getInviteeTimezone(), booking.getJoinUrl(), booking.getHostUrl(), booking.getCreatedAt());
    }
}