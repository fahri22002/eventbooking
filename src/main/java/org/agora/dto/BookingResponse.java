package org.agora.dto;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

public record BookingResponse(
        String bookingId,
        String eventId,
        String eventTitle,
        String bookingReference,
        int quantity,
        BigDecimal totalPrice,
        String status,
        ZonedDateTime bookingDateTime,
        ZonedDateTime eventDateTime
) {}