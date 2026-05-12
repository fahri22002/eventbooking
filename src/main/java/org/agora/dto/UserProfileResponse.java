package org.agora.dto;

import lombok.Data;
import lombok.AllArgsConstructor;


@AllArgsConstructor
@Data
public class UserProfileResponse {
    private String userId;
    private String name;
    private String email;
}
