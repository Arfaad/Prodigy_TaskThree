package com.example.taskone.controller;

import com.example.taskone.model.User;
import com.example.taskone.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping({"/api/users", "/api/users/"})
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * Retrieves all users currently registered in the database.
     * 
     * @return A ResponseEntity containing the list of all Users and HTTP status 200 (OK).
     */
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    /**
     * Retrieves a single user by their unique identifier (UUID).
     * 
     * @param id The UUID of the user to retrieve.
     * @return A ResponseEntity containing the requested User and HTTP status 200 (OK).
     */
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    /**
     * Registers and creates a new user in the database.
     * Performs validation on user details and verifies email uniqueness.
     * 
     * @param user The User details to create.
     * @return A ResponseEntity containing the created User with its assigned UUID and HTTP status 201 (Created).
     */
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        User createdUser = userService.createUser(user);
        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
    }

    /**
     * Updates the details of an existing user identified by their UUID.
     * Validates input details and ensures the new email is not already taken by another user.
     * 
     * @param id The UUID of the user to update.
     * @param user The updated User details.
     * @return A ResponseEntity containing the updated User and HTTP status 200 (OK).
     */
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable UUID id, @RequestBody User user) {
        User updatedUser = userService.updateUser(id, user);
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * Deletes a user from the database identified by their UUID.
     * 
     * @param id The UUID of the user to delete.
     * @return A ResponseEntity with HTTP status 244 (No Content).
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
