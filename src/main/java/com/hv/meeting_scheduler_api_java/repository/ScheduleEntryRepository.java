package com.hv.meeting_scheduler_api_java.repository;

import com.hv.meeting_scheduler_api_java.domain.ScheduleEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface ScheduleEntryRepository extends JpaRepository<ScheduleEntry, UUID> {

    List<ScheduleEntry> findByScheduleId(UUID scheduleId);

    List<ScheduleEntry> findByScheduleIdIn(Collection<UUID> scheduleIds);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from ScheduleEntry e where e.schedule.id = :scheduleId")
    void deleteAllByScheduleId(@Param("scheduleId") UUID scheduleId);
}