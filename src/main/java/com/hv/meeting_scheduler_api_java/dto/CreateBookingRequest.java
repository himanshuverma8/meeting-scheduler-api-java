package com.hv.meeting_scheduler_api_java.dto;

import com.hv.meeting_scheduler_api_java.validation.ValidTimezone;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateBookingRequest(
        @NotNull UUID eventTypeId,
        @NotBlank String startTime,
        @NotBlank String inviteeName,
        @NotBlank @Email String inviteeEmail,
        @NotBlank @ValidTimezone String inviteeTimezone
) {
}
