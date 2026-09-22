package com.hv.meeting_scheduler_api_java.domain;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "host_id", nullable = false)
    private User host;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_type_id", nullable = false)
    private EventType eventType;

    @Column(name = "idempotency_key", unique = true)
    private String idempotencyKey;

    @Column(name = "start_time", nullable = false)
    private Instant startTime;

    @Column(name = "end_time", nullable = false)
    private Instant endTime;

    @Column(nullable = false)
    private Integer duration;

    @Column(name = "invitee_name", nullable = false)
    private String inviteeName;

    @Column(name = "invitee_email", nullable = false)
    private String inviteeEmail;

    @Column(name = "invitee_timezone", nullable = false)
    private String inviteeTimezone;

    @Column(name = "join_url")
    private String joinUrl;

    @Column(name = "host_url")
    private String hostUrl;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected Booking() {}

    public UUID getId() {
        return id;
    }

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    public Instant getStartTime() {
        return startTime;
    }

    public void setStartTime(Instant startTime) {
        this.startTime = startTime;
    }

    public Instant getEndTime() {
        return endTime;
    }

    public void setEndTime(Instant endTime) {
        this.endTime = endTime;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public User getHost() {
        return host;
    }

    public void setHost(User host) {
        this.host = host;
    }

    public String getInviteeName() {
        return inviteeName;
    }

    public void setInviteeName(String inviteeName) {
        this.inviteeName = inviteeName;
    }

    public String getInviteeEmail() {
        return inviteeEmail;
    }

    public void setInviteeEmail(String inviteeEmail) {
        this.inviteeEmail = inviteeEmail;
    }

    public String getInviteeTimezone() {
        return inviteeTimezone;
    }

    public void setInviteeTimezone(String inviteeTimezone) {
        this.inviteeTimezone = inviteeTimezone;
    }

    public String getJoinUrl() {
        return joinUrl;
    }

    public void setJoinUrl(String joinUrl) {
        this.joinUrl = joinUrl;
    }

    public String getHostUrl() {
        return hostUrl;
    }

    public void setHostUrl(String hostUrl) {
        this.hostUrl = hostUrl;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Booking(User host, EventType eventType, String idempotencyKey, Instant startTime, Instant endTime, Integer duration, String inviteeName, String inviteeEmail, String inviteeTimezone, String joinUrl, String hostUrl) {
        this.host = host;
        this.eventType = eventType;
        this.idempotencyKey = idempotencyKey;
        this.startTime = startTime;
        this.endTime = endTime;
        this.duration = duration;
        this.inviteeName = inviteeName;
        this.inviteeEmail = inviteeEmail;
        this.inviteeTimezone = inviteeTimezone;
        this.joinUrl = joinUrl;
        this.hostUrl = hostUrl;
    }
}
