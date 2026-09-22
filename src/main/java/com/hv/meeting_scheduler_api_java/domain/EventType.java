package com.hv.meeting_scheduler_api_java.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "event_types")
public class EventType {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id", nullable = false)
    private Schedule schedule;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer duration;

    @Column(name = "buffer_before", nullable = false)
    private Integer bufferBefore;

    @Column(name = "buffer_after", nullable = false)
    private Integer bufferAfter;

    @Column(name = "min_notice_minutes", nullable = false)
    private Integer minNoticeMinutes;

    @Column(name = "max_days_in_advance", nullable = false)
    private Integer maxDaysInAdvance;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected EventType() {}

    public UUID getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Schedule getSchedule() {
        return schedule;
    }

    public void setSchedule(Schedule schedule) {
        this.schedule = schedule;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getBufferBefore() {
        return bufferBefore;
    }

    public void setBufferBefore(Integer bufferBefore) {
        this.bufferBefore = bufferBefore;
    }

    public Integer getBufferAfter() {
        return bufferAfter;
    }

    public void setBufferAfter(Integer bufferAfter) {
        this.bufferAfter = bufferAfter;
    }

    public Integer getMinNoticeMinutes() {
        return minNoticeMinutes;
    }

    public void setMinNoticeMinutes(Integer minNoticeMinutes) {
        this.minNoticeMinutes = minNoticeMinutes;
    }

    public Integer getMaxDaysInAdvance() {
        return maxDaysInAdvance;
    }

    public void setMaxDaysInAdvance(Integer maxDaysInAdvance) {
        this.maxDaysInAdvance = maxDaysInAdvance;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public EventType(User user, Schedule schedule, String name, Integer duration, Integer bufferBefore, Integer bufferAfter, Integer minNoticeMinutes, Integer maxDaysInAdvance, Instant updatedAt) {
        this.user = user;
        this.schedule = schedule;
        this.name = name;
        this.duration = duration;
        this.bufferBefore = bufferBefore;
        this.bufferAfter = bufferAfter;
        this.minNoticeMinutes = minNoticeMinutes;
        this.maxDaysInAdvance = maxDaysInAdvance;
        this.updatedAt = updatedAt;
    }
}
