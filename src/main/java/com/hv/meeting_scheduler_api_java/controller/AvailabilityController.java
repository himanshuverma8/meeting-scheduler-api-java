package com.hv.meeting_scheduler_api_java.controller;

import com.hv.meeting_scheduler_api_java.dto.AvailabilityResponse;
import com.hv.meeting_scheduler_api_java.service.AvailabilityService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/availability")
public class AvailabilityController {

    private final AvailabilityService availabilityService;

    public AvailabilityController(AvailabilityService availabilityService) {
        this.availabilityService = availabilityService;
    }

    @GetMapping
    public AvailabilityResponse getAvailableSlots(
            @RequestParam UUID eventTypeId,
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam String inviteeTimezone
    ) {
        return availabilityService.getAvailableSlots(eventTypeId, startDate, endDate, inviteeTimezone);
    }
}