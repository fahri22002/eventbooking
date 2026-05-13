package org.agora.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

public record EventRequest(
        @NotBlank(message = "Title is required")
        String title,

        @NotBlank(message = "Description is required")
        String description,

        @NotBlank(message = "Location is required")
        String location,

        @NotNull(message = "Datetime is required")
        @Future(message = "Event dateTime must be in the future")
        ZonedDateTime dateTime,

        @Min(value = 1, message = "Seat quota must be at least 1")
        int seatQuota,

        @Min(value = 0, message = "Price cannot be negative")
        BigDecimal price
) {}