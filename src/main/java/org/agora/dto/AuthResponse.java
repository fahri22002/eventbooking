package org.agora.dto;

/**
 * DTO for returning the authentication token.
 * Sent to the client upon successful login or registration.
 */
public record AuthResponse (
    String token
){}
