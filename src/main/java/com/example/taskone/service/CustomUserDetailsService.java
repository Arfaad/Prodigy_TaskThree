package com.example.taskone.service;

import com.example.taskone.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Custom implementation of UserDetailsService to fetch User details from the database
 * using their email address. Used by Spring Security authentication providers.
 * Caches loaded users under "usersByEmail" to optimize security authentication lookups.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Autowired
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Loads a user by their email address. Caches the result under "usersByEmail".
     * 
     * @param username The email address identifying the user.
     * @return UserDetails principal object.
     * @throws UsernameNotFoundException if user is not found in the database.
     */
    @Override
    @Cacheable(value = "usersByEmail", key = "#username")
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + username));
    }
}
