package com.example.taskone.dto;

import com.example.taskone.model.Role;
import lombok.*;

/**
 * Data Transfer Object representing user registration requests.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    private String name;
    private String email;
    private Integer age;
    private String password;
    private Role role; // Optional, default is assigned in controller/service
}
