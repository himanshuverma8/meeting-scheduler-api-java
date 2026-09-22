package com.hv.meeting_scheduler_api_java.domain;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "bookings")
public class Bookings {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private EventType eventType;

}
