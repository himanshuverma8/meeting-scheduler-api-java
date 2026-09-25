package com.hv.meeting_scheduler_api_java.service;

import com.hv.meeting_scheduler_api_java.domain.Schedule;
import com.hv.meeting_scheduler_api_java.domain.ScheduleEntry;
import com.hv.meeting_scheduler_api_java.domain.User;
import com.hv.meeting_scheduler_api_java.dto.CreateScheduleRequest;
import com.hv.meeting_scheduler_api_java.dto.ScheduleEntryRequest;
import com.hv.meeting_scheduler_api_java.dto.ScheduleEntryResponse;
import com.hv.meeting_scheduler_api_java.dto.ScheduleResponse;
import com.hv.meeting_scheduler_api_java.exception.ResourceNotFoundException;
import com.hv.meeting_scheduler_api_java.repository.ScheduleEntryRepository;
import com.hv.meeting_scheduler_api_java.repository.ScheduleRepository;
import com.hv.meeting_scheduler_api_java.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final UserRepository userRepository;
    private final ScheduleEntryRepository scheduleEntryRepository;

    public ScheduleService(ScheduleRepository scheduleRepository, UserRepository userRepository, ScheduleEntryRepository scheduleEntryRepository) {
        this.scheduleRepository = scheduleRepository;
        this.userRepository = userRepository;
        this.scheduleEntryRepository = scheduleEntryRepository;
    }

    @Transactional
    public ScheduleResponse create(UUID userId, CreateScheduleRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("user not found"));

        Schedule schedule = scheduleRepository.save(
                new Schedule(user, request.name(), request.timezone()));

        List<ScheduleEntry> entries = saveEntries(schedule, request.entries());
        return toResponse(schedule, entries);
    }

    @Transactional(readOnly = true)
    public List<ScheduleResponse> listForUser(UUID userId) {
        List<Schedule> schedules = scheduleRepository.findByUserId(userId);
        if (schedules.isEmpty()) {
            return List.of();
        }
        List<UUID> ids = schedules.stream().map(Schedule::getId).toList();
        Map<UUID, List<ScheduleEntry>> entriesBySchedule =
                scheduleEntryRepository.findByScheduleIdIn(ids).stream()
                        .collect(Collectors.groupingBy(e -> e.getSchedule().getId()));

        return schedules.stream()
                .map(s -> toResponse(s, entriesBySchedule.getOrDefault(s.getId(), List.of())))
                .toList();
    }

    @Transactional(readOnly = true)
    public ScheduleResponse getOwned(UUID userId, UUID scheduleId) {
        Schedule schedule = findOwnedOrThrow(userId, scheduleId);
        return toResponse(schedule, scheduleEntryRepository.findByScheduleId(scheduleId));
    }

    @Transactional
    public ScheduleResponse update(UUID userId, UUID scheduleId, CreateScheduleRequest request) {
        Schedule schedule = findOwnedOrThrow(userId, scheduleId);
        schedule.update(request.name(), request.timezone());

        scheduleEntryRepository.deleteAllByScheduleId(scheduleId);
        List<ScheduleEntry> entries = saveEntries(schedule, request.entries());
        return toResponse(schedule, entries);
    }

    @Transactional
    public void deleteOwned(UUID userId, UUID scheduleId) {
        Schedule schedule = findOwnedOrThrow(userId, scheduleId);
        scheduleRepository.delete(schedule);
    }

    private Schedule findOwnedOrThrow(UUID userId, UUID scheduleId) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("schedule not found"));

        if (!schedule.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("schedule not found");
        }

        return schedule;
    }

    private List<ScheduleEntry> saveEntries(Schedule schedule, List<ScheduleEntryRequest> requests) {
        List<ScheduleEntry> entries = requests.stream()
                .map(r -> new ScheduleEntry(schedule, r.dayOfWeek(), r.specificDate(), r.startTime(), r.endTime()))
                .toList();
        return scheduleEntryRepository.saveAll(entries);
    }

    private ScheduleResponse toResponse(Schedule schedule, List<ScheduleEntry> entries) {

        List<ScheduleEntryResponse> entryResponses = entries.stream()
                .map(e -> new ScheduleEntryResponse(e.getId(), e.getDayOfWeek(),
                        e.getSpecificDate(), e.getStartTime(), e.getEndTime()))
                .toList();


        return new ScheduleResponse(
                schedule.getId(),
                schedule.getName(),
                schedule.getTimezone(),
                schedule.getCreatedAt(),
                schedule.getUpdatedAt(),
                entryResponses
        );
    }


}
