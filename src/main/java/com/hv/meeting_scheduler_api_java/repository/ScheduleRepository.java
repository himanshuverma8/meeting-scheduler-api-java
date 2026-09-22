package com.hv.meeting_scheduler_api_java.repository;

import com.hv.meeting_scheduler_api_java.domain.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ScheduleRepository extends JpaRepository<Schedule, UUID> {
}
