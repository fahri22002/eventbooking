package org.agora.dto;

import java.time.ZonedDateTime;

/**
 * DTO for standardizing API error responses.
 * Provides a consistent structure containing the error status, timestamp, and specific error details.
 */
public record ErrorResponse(
        ZonedDateTime timestamp,
        int status,
        String error,
        // Object allows for either a single String message or a Map of field validation errors
        Object message,
        String path
) {}