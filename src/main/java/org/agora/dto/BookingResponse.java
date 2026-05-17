package org.agora.dto;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

/**
 * DTO representing a booking confirmation.
 * Returns comprehensive details about the user's reservation, including the unique reference code.
 */
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