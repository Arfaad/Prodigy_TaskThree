package com.example.taskone.service;

import com.example.taskone.exception.UserNotFoundException;
import com.example.taskone.exception.ValidationException;
import com.example.taskone.model.Role;
import com.example.taskone.model.User;
import com.example.taskone.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Service class handling core business logic for user management operations (CRUD).
 * Integrates password hashing, role assignment, and validation checks.
 * Optimized with declarative Spring Caching to minimize redundant SQL queries.
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    // Simple email validation regex pattern
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
    );

    /**
     * Constructor injection of dependencies.
     * 
     * @param userRepository The database repository handler.
     * @param passwordEncoder The BCrypt password hashing handler.
     */
    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Fetches all registered users from the database. Cached under "usersList".
     * Transaction is read-only for performance optimization.
     * 
     * @return List of Users in the database.
     */
    @Cacheable(value = "usersList")
    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Fetches a specific user by their unique identifier (UUID). Cached under "users".
     * Throws an exception if the user does not exist.
     * Transaction is read-only for performance optimization.
     * 
     * @param id The UUID of the user to fetch.
     * @return The User if found.
     * @throws UserNotFoundException if no user matches the given UUID.
     */
    @Cacheable(value = "users", key = "#id")
    @Transactional(readOnly = true)
    public User getUserById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User with ID " + id + " not found"));
    }

    /**
     * Validates and saves a new user in the database.
     * Puts the newly created user in the "users" cache and evicts the "usersList" and "usersByEmail" caches.
     * 
     * @param user The User information containing name, email, age, and password.
     * @return The saved User entity.
     * @throws ValidationException if validations fail or the email is already registered.
     */
    @CachePut(value = "users", key = "#result.id")
    @Caching(evict = {
        @CacheEvict(value = "usersList", allEntries = true),
        @CacheEvict(value = "usersByEmail", allEntries = true)
    })
    @Transactional
    public User createUser(User user) {
        validateUser(user);
        
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new ValidationException("Email is already in use");
        }

        // Validate password specifically for new users
        if (user.getPassword() == null || user.getPassword().trim().length() < 6) {
            throw new ValidationException("Password is required and must be at least 6 characters");
        }
        
        UUID id = UUID.randomUUID();
        user.setId(id);
        // Encode password securely
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        // Assign default role if none provided
        if (user.getRole() == null) {
            user.setRole(Role.USER);
        }
        return userRepository.save(user);
    }

    /**
     * Updates an existing user's details.
     * Updates the "users" cache with the new user details and evicts the "usersList" and "usersByEmail" caches.
     * 
     * @param id The UUID of the user to update.
     * @param userDetails The new User details.
     * @return The updated User entity.
     * @throws UserNotFoundException if the user does not exist.
     * @throws ValidationException if input details are invalid or the email is taken by another user.
     */
    @CachePut(value = "users", key = "#id")
    @Caching(evict = {
        @CacheEvict(value = "usersList", allEntries = true),
        @CacheEvict(value = "usersByEmail", allEntries = true)
    })
    @Transactional
    public User updateUser(UUID id, User userDetails) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User with ID " + id + " not found"));
        
        validateUser(userDetails);
        
        if (userRepository.existsByEmailAndIdNot(userDetails.getEmail(), id)) {
            throw new ValidationException("Email is already in use");
        }
        
        userDetails.setId(id); // Ensure the ID remains unchanged
        
        // Handle password update: if a new password is provided, encode it; otherwise keep the existing password
        if (userDetails.getPassword() != null && !userDetails.getPassword().trim().isEmpty()) {
            if (userDetails.getPassword().trim().length() < 6) {
                throw new ValidationException("Password must be at least 6 characters");
            }
            userDetails.setPassword(passwordEncoder.encode(userDetails.getPassword()));
        } else {
            userDetails.setPassword(existingUser.getPassword());
        }

        // Keep the role of the user if not specified in update
        if (userDetails.getRole() == null) {
            userDetails.setRole(existingUser.getRole());
        }

        return userRepository.save(userDetails);
    }

    /**
     * Deletes a user by their UUID.
     * Evicts the deleted user from "users" and "usersByEmail" caches, and invalidates "usersList" cache.
     * 
     * @param id The UUID of the user to delete.
     * @throws UserNotFoundException if the user does not exist.
     */
    @Caching(evict = {
        @CacheEvict(value = "users", key = "#id"),
        @CacheEvict(value = "usersList", allEntries = true),
        @CacheEvict(value = "usersByEmail", allEntries = true)
    })
    @Transactional
    public void deleteUser(UUID id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException("User with ID " + id + " not found");
        }
        userRepository.deleteById(id);
    }

    /**
     * Performs validations on the user fields.
     * Validates name is not empty, email matches standard format, and age is a positive integer under 150.
     * 
     * @param user The User entity to validate.
     * @throws ValidationException if any validation condition is violated.
     */
    private void validateUser(User user) {
        if (user == null) {
            throw new ValidationException("User request body cannot be null");
        }

        // Validate Name
        if (user.getName() == null || user.getName().trim().isEmpty()) {
            throw new ValidationException("Name is required and cannot be empty");
        }

        // Validate Email
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            throw new ValidationException("Email is required and cannot be empty");
        }
        if (!EMAIL_PATTERN.matcher(user.getEmail()).matches()) {
            throw new ValidationException("Email is invalid");
        }

        // Validate Age
        if (user.getAge() == null) {
            throw new ValidationException("Age is required");
        }
        if (user.getAge() <= 0) {
            throw new ValidationException("Age must be a positive integer");
        }
        if (user.getAge() > 150) {
            throw new ValidationException("Age must be realistic (less than 150)");
        }
    }
}
