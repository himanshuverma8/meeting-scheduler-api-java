package com.hv.meeting_scheduler_api_java.service;

import com.hv.meeting_scheduler_api_java.domain.Schedule;
import com.hv.meeting_scheduler_api_java.domain.User;
import com.hv.meeting_scheduler_api_java.dto.CreateScheduleRequest;
import com.hv.meeting_scheduler_api_java.dto.ScheduleResponse;
import com.hv.meeting_scheduler_api_java.exception.ResourceNotFoundException;
import com.hv.meeting_scheduler_api_java.repository.ScheduleRepository;
import com.hv.meeting_scheduler_api_java.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final UserRepository userRepository;

    private ScheduleResponse toResponse(Schedule schedule) {
        return new ScheduleResponse(
                schedule.getId(),
                schedule.getName(),
                schedule.getTimezone(),
                schedule.getCreatedAt(),
                schedule.getUpdatedAt()
        );
    }

    public ScheduleService(ScheduleRepository scheduleRepository, UserRepository userRepository) {
        this.scheduleRepository = scheduleRepository;
        this.userRepository = userRepository;
    }

    public ScheduleResponse create(UUID userId, CreateScheduleRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("user not found"));

        Schedule schedule = new Schedule(user, request.name(), request.timezone());
        Schedule saved = scheduleRepository.save(schedule);
        return toResponse(saved);
    }

    public List<ScheduleResponse> listForUser(UUID userid) {
        return scheduleRepository.findByUserId(userid).stream()
                .map(this::toResponse)
                .toList();
    }

    public ScheduleResponse getOwned(UUID userId, UUID scheduleId) {
        Schedule schedule = findOwnedOrThrow(userId, scheduleId);
        return toResponse(schedule);
    }

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


}
