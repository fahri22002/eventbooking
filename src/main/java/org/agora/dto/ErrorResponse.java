package org.agora.dto;

import java.time.ZonedDateTime;

public record ErrorResponse(
        ZonedDateTime timestamp,
        int status,
        String error,
        Object message,
        String path
) {}