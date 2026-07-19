package com.example.taskone.service;

import com.example.taskone.exception.UserNotFoundException;
import com.example.taskone.exception.ValidationException;
import com.example.taskone.model.User;
import com.example.taskone.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class UserService {

    private final UserRepository userRepository;
    
    // Simple email validation regex pattern
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
    );

    /**
     * Constructor injection of the UserRepository.
     * 
     * @param userRepository The database repository handler.
     */
    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Fetches all registered users from the database.
     * Transaction is read-only for performance optimization.
     * 
     * @return List of Users in the database.
     */
    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Fetches a specific user by their unique identifier (UUID).
     * Throws an exception if the user does not exist.
     * Transaction is read-only for performance optimization.
     * 
     * @param id The UUID of the user to fetch.
     * @return The User if found.
     * @throws UserNotFoundException if no user matches the given UUID.
     */
    @Transactional(readOnly = true)
    public User getUserById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User with ID " + id + " not found"));
    }

    /**
     * Validates and saves a new user in the database.
     * Generates a new random UUID for the user and verifies email uniqueness.
     * 
     * @param user The User information containing name, email, and age.
     * @return The saved User entity.
     * @throws ValidationException if validations fail or the email is already registered.
     */
    @Transactional
    public User createUser(User user) {
        validateUser(user);
        
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new ValidationException("Email is already in use");
        }
        
        UUID id = UUID.randomUUID();
        user.setId(id);
        return userRepository.save(user);
    }

    /**
     * Updates an existing user's details.
     * Validates that the user exists, verifies details, and ensures email uniqueness.
     * 
     * @param id The UUID of the user to update.
     * @param userDetails The new User details.
     * @return The updated User entity.
     * @throws UserNotFoundException if the user does not exist.
     * @throws ValidationException if input details are invalid or the email is taken by another user.
     */
    @Transactional
    public User updateUser(UUID id, User userDetails) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException("User with ID " + id + " not found");
        }
        
        validateUser(userDetails);
        
        if (userRepository.existsByEmailAndIdNot(userDetails.getEmail(), id)) {
            throw new ValidationException("Email is already in use");
        }
        
        userDetails.setId(id); // Ensure the ID remains unchanged
        return userRepository.save(userDetails);
    }

    /**
     * Deletes a user by their UUID.
     * 
     * @param id The UUID of the user to delete.
     * @throws UserNotFoundException if the user does not exist.
     */
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
