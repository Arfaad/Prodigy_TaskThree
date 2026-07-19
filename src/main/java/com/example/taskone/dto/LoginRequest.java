package com.example.taskone.dto;

import lombok.*;

/**
 * Data Transfer Object representing user login credential requests.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {
    private String email;
    private String password;
}
