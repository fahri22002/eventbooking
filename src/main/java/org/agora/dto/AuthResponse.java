package org.agora.dto;

import lombok.Data;
import lombok.AllArgsConstructor;

/**
 * DTO for returning the authentication token.
 * Sent to the client upon successful login or registration.
 */
@AllArgsConstructor
@Data
public class AuthResponse {
    private String token;
}
