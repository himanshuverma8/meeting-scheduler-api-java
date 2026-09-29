package com.hv.meeting_scheduler_api_java.repository;

import com.hv.meeting_scheduler_api_java.domain.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BookingRepository extends JpaRepository<Booking, UUID> {

    List<Booking> findByHostId(UUID hostId);

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    Optional<Booking> findByIdempotencyKey(String idempotencyKey);
}
