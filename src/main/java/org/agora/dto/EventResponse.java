package org.agora.dto;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

/**
 * DTO for returning event details to the client.
 * Safely exposes event information, including seat availability and creator details, without leaking internal entities.
 */
public record EventResponse(
        String eventId,
        String title,
        String description,
        String location,
        ZonedDateTime dateTime,
        int seatQuota,
        int seatsAvailable,
        BigDecimal price,
        boolean isActive,
        String creatorName
) {}