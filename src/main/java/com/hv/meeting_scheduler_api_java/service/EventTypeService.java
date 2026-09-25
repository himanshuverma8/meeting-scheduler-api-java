package com.hv.meeting_scheduler_api_java.service;

import com.hv.meeting_scheduler_api_java.domain.EventType;
import com.hv.meeting_scheduler_api_java.domain.Schedule;
import com.hv.meeting_scheduler_api_java.domain.User;
import com.hv.meeting_scheduler_api_java.dto.CreateEventTypeRequest;
import com.hv.meeting_scheduler_api_java.dto.EventTypeResponse;
import com.hv.meeting_scheduler_api_java.dto.UpdateEventTypeRequest;
import com.hv.meeting_scheduler_api_java.exception.ResourceNotFoundException;
import com.hv.meeting_scheduler_api_java.repository.EventTypeRepository;
import com.hv.meeting_scheduler_api_java.repository.ScheduleRepository;
import com.hv.meeting_scheduler_api_java.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class EventTypeService {

    private final ScheduleRepository scheduleRepository;
    private final UserRepository userRepository;
    private final EventTypeRepository eventTypeRepository;

    public EventTypeService(EventTypeRepository eventTypeRepository, ScheduleRepository scheduleRepository, UserRepository userRepository) {
        this.eventTypeRepository = eventTypeRepository;
        this.scheduleRepository = scheduleRepository;
        this.userRepository = userRepository;
    }

    public EventTypeResponse create(UUID userId, CreateEventTypeRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("user not found"));
        Schedule schedule = scheduleRepository.findById(request.scheduleId())
                .orElseThrow(() -> new ResourceNotFoundException("schedule not found"));

        if (!schedule.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("schedule not found");
        }

        EventType eventType = new EventType(
                user, schedule, request.name(), request.duration(),
                request.bufferBefore(), request.bufferAfter(),
                request.minNoticeMinutes(), request.maxDaysInAdvance());

        EventType saved = eventTypeRepository.save(eventType);
        return toResponse(saved);
    }

    public List<EventTypeResponse> listForUser(UUID userId) {
        return eventTypeRepository.findByUserId(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    public EventTypeResponse getOwned(UUID userId, UUID eventTypeId) {
        EventType eventType = findOwnedOrThrow(userId, eventTypeId);
        return toResponse(eventType);
    }

    @Transactional
    public EventTypeResponse update(UUID userId, UUID eventTypeId, UpdateEventTypeRequest request) {
        EventType eventType = findOwnedOrThrow(userId, eventTypeId);

        if (request.scheduleId() != null) {
            Schedule schedule = scheduleRepository.findById(request.scheduleId())
                    .orElseThrow(() -> new ResourceNotFoundException("schedule not found"));
            if (!schedule.getUser().getId().equals(userId)) {
                throw new ResourceNotFoundException("schedule not found");
            }
            eventType.setSchedule(schedule);
        }
        if (request.name() != null) eventType.setName(request.name());
        if (request.duration() != null) eventType.setDuration(request.duration());
        if (request.bufferBefore() != null) eventType.setBufferBefore(request.bufferBefore());
        if (request.bufferAfter() != null) eventType.setBufferAfter(request.bufferAfter());
        if (request.minNoticeMinutes() != null) eventType.setMinNoticeMinutes(request.minNoticeMinutes());
        if (request.maxDaysInAdvance() != null) eventType.setMaxDaysInAdvance(request.maxDaysInAdvance());

        eventType.setUpdatedAt(Instant.now());
        return toResponse(eventType);
    }

    public void deleteOwned(UUID userId, UUID eventTypeId) {
        EventType eventType = findOwnedOrThrow(userId, eventTypeId);
        eventTypeRepository.delete(eventType);
    }

    private EventType findOwnedOrThrow(UUID userId, UUID evenTypeId) {
        EventType eventType = eventTypeRepository.findById(evenTypeId)
                .orElseThrow(() -> new ResourceNotFoundException("event type not found"));

        if (!eventType.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("event type not found");
        }
        return eventType;
    }

    private EventTypeResponse toResponse(EventType eventType) {
        return new EventTypeResponse(
                eventType.getId(),
                eventType.getSchedule().getId(),
                eventType.getName(),
                eventType.getDuration(),
                eventType.getBufferBefore(),
                eventType.getBufferAfter(),
                eventType.getMinNoticeMinutes(),
                eventType.getMaxDaysInAdvance(),
                eventType.getCreatedAt(),
                eventType.getUpdatedAt()
        );
    }
}
