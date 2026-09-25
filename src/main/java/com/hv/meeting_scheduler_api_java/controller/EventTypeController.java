package com.hv.meeting_scheduler_api_java.controller;

import com.hv.meeting_scheduler_api_java.dto.CreateEventTypeRequest;
import com.hv.meeting_scheduler_api_java.dto.EventTypeResponse;
import com.hv.meeting_scheduler_api_java.dto.UpdateEventTypeRequest;
import com.hv.meeting_scheduler_api_java.service.EventTypeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/event-types")
public class EventTypeController {

    private final EventTypeService eventTypeService;

    public EventTypeController(EventTypeService eventTypeService) {
        this.eventTypeService = eventTypeService;
    }

    @PostMapping
    public ResponseEntity<EventTypeResponse> create(
            @AuthenticationPrincipal UUID userId,
            @Valid @RequestBody CreateEventTypeRequest request
    ) {
        EventTypeResponse response = eventTypeService.create(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public List<EventTypeResponse> list(@AuthenticationPrincipal UUID userId) {
        return eventTypeService.listForUser(userId);
    }

    @GetMapping("/{id}")
    public EventTypeResponse get(@AuthenticationPrincipal UUID userId, @PathVariable UUID id) {
        return eventTypeService.getOwned(userId, id);
    }

    @PatchMapping("/{id}")
    public EventTypeResponse update(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateEventTypeRequest request
    ) {
        return eventTypeService.update(userId, id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal UUID userId, @PathVariable UUID id) {
        eventTypeService.deleteOwned(userId, id);
        return ResponseEntity.noContent().build();
    }
}
