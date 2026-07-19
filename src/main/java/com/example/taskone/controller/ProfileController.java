package com.example.taskone.controller;

import com.example.taskone.model.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller mapping requests for retrieving profile details of the currently authenticated user.
 */
@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    /**
     * Retrieves the profile of the currently logged-in user.
     * Uses Spring Security's AuthenticationPrincipal context binder.
     * 
     * @param user The authenticated user principal details.
     * @return A ResponseEntity containing the User profile.
     */
    @GetMapping
    public ResponseEntity<User> getProfile(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(user);
    }
}
