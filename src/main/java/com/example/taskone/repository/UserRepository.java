package com.example.taskone.repository;

import com.example.taskone.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    
    /**
     * Finds a user by their email address.
     * 
     * @param email The user's email address.
     * @return An Optional containing the User if found, otherwise empty.
     */
    Optional<User> findByEmail(String email);

    /**
     * Checks if a user with the specified email already exists.
     * Used during user creation.
     */
    boolean existsByEmail(String email);

    /**
     * Checks if a user with the specified email exists, excluding the user with the given ID.
     * Used during user updates.
     */
    boolean existsByEmailAndIdNot(String email, UUID id);
}
