package org.agora.dto;

import java.time.ZonedDateTime;

/**
 * DTO for returning user profile details.
 * Safely exposes account information to the client without revealing sensitive data such as passwords.
 */
public record UserProfileResponse (
    String userId,
    String name,
    String email,
    ZonedDateTime createAt
){}
