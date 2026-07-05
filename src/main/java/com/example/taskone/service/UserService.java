package com.example.taskone.service;

import com.example.taskone.exception.UserNotFoundException;
import com.example.taskone.exception.ValidationException;
import com.example.taskone.model.User;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

@Service
public class UserService {

    private final Map<UUID, User> userStorage = new ConcurrentHashMap<>();
    
    // Simple email validation regex pattern
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
    );

    public List<User> getAllUsers() {
        return new ArrayList<>(userStorage.values());
    }

    public User getUserById(UUID id) {
        User user = userStorage.get(id);
        if (user == null) {
            throw new UserNotFoundException("User with ID " + id + " not found");
        }
        return user;
    }

    public User createUser(User user) {
        validateUser(user);
        
        UUID id = UUID.randomUUID();
        user.setId(id);
        userStorage.put(id, user);
        return user;
    }

    public User updateUser(UUID id, User userDetails) {
        if (!userStorage.containsKey(id)) {
            throw new UserNotFoundException("User with ID " + id + " not found");
        }
        
        validateUser(userDetails);
        
        userDetails.setId(id); // Ensure the ID remains unchanged
        userStorage.put(id, userDetails);
        return userDetails;
    }

    public void deleteUser(UUID id) {
        if (!userStorage.containsKey(id)) {
            throw new UserNotFoundException("User with ID " + id + " not found");
        }
        userStorage.remove(id);
    }

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
