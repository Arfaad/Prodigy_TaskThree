package com.example.taskone.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Controller mapping administrative operations, secured by role-based access control (RBAC).
 * Class-level annotation requires the request principal to have the ADMIN role.
 */
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    /**
     * Accesses the administrative dashboard.
     * Secured using method-level security checked against the principal's roles.
     * 
     * @return A welcome message map with HTTP status 200 (OK).
     */
    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, String>> getDashboard() {
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Welcome Admin! This is the secured administrative dashboard."
        ));
    }
}
