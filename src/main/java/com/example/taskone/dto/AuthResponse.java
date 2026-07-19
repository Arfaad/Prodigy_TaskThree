package com.example.taskone.dto;

import com.example.taskone.model.Role;
import lombok.*;

import java.util.UUID;

/**
 * Data Transfer Object representing successful authentication tokens and principal profile info responses.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String token;
    @Builder.Default
    private String type = "Bearer";
    private UUID id;
    private String name;
    private String email;
    private Role role;
}
