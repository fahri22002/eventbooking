package org.agora.dto;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

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