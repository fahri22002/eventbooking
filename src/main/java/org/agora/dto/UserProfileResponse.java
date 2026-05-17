package org.agora.dto;

import lombok.Data;
import lombok.AllArgsConstructor;

import java.time.ZonedDateTime;

/**
 * DTO for returning user profile details.
 * Safely exposes account information to the client without revealing sensitive data such as passwords.
 */
@AllArgsConstructor
@Data
public class UserProfileResponse {
    private String userId;
    private String name;
    private String email;
    private ZonedDateTime createAt;
}
