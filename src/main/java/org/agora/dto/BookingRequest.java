package org.agora.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO for incoming ticket booking requests.
 * Includes validation to ensure a valid event ID and at least one ticket is requested.
 */
public record BookingRequest(
        @NotBlank(message = "Event ID is required")
        String eventId,

        @Min(value = 1, message = "Quantity must be at least 1")
        int quantity
) {}