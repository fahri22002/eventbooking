package org.agora.dto;

import lombok.Data;
import lombok.AllArgsConstructor;

import java.time.ZonedDateTime;


@AllArgsConstructor
@Data
public class UserProfileResponse {
    private String userId;
    private String name;
    private String email;
    private ZonedDateTime createAt;
}
