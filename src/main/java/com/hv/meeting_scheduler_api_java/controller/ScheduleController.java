package com.hv.meeting_scheduler_api_java.controller;

import com.hv.meeting_scheduler_api_java.domain.Schedule;
import com.hv.meeting_scheduler_api_java.dto.CreateScheduleRequest;
import com.hv.meeting_scheduler_api_java.dto.ScheduleResponse;
import com.hv.meeting_scheduler_api_java.service.ScheduleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("schedules")
public class ScheduleController {

    private final ScheduleService scheduleService;

    public ScheduleController(ScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    @PostMapping
    public ResponseEntity<ScheduleResponse> create(
            @AuthenticationPrincipal UUID userId,
            @Valid @RequestBody CreateScheduleRequest request
            ) {
        ScheduleResponse response = scheduleService.create(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public List<ScheduleResponse> list(@AuthenticationPrincipal UUID userId) {
        return scheduleService.listForUser(userId);
    }

   @GetMapping("/{id}")
    public ScheduleResponse get(@AuthenticationPrincipal UUID userId, @PathVariable UUID id) {
        return scheduleService.getOwned(userId, id);
   }

   @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal UUID userId, @PathVariable UUID id) {
        scheduleService.deleteOwned(userId, id);
        return ResponseEntity.noContent().build();
   }

   @PutMapping("/{id}")
    public ScheduleResponse update(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID id,
            @Valid @RequestBody CreateScheduleRequest request
   ) {
        return scheduleService.update(userId, id, request);
   }

}
