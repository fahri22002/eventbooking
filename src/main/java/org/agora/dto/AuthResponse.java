package org.agora.dto;

import lombok.Data;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@Data
public class AuthResponse {
    private String token;
}
