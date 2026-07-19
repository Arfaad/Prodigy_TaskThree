package com.example.taskone.security;

import com.example.taskone.model.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.logging.Logger;

/**
 * Utility class for handling JSON Web Tokens (JWT) using the modern io.jsonwebtoken (JJWT) API style (0.12.x).
 * Provides methods for token generation, parsing, validation, and claim extraction.
 */
@Component
public class JwtUtils {

    private static final Logger LOGGER = Logger.getLogger(JwtUtils.class.getName());

    @Value("${app.security.jwt.secret}")
    private String jwtSecret;

    @Value("${app.security.jwt.expiration}")
    private long jwtExpirationMs;

    private SecretKey key;

    /**
     * Initializes the cryptographic SecretKey from the configured JWT secret string.
     * Ensures the key has sufficient length for HMAC-SHA256.
     */
    @PostConstruct
    public void init() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Generates a signed JWT for the authenticated user, storing their UUID and role as claims.
     * 
     * @param user The authenticated user principal details.
     * @return A signed JWT string.
     */
    public String generateToken(User user) {
        return Jwts.builder()
                .subject(user.getEmail())
                .claim("id", user.getId().toString())
                .claim("role", user.getRole().name())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(key)
                .compact();
    }

    /**
     * Extracts the user's email/username from the JWT payload.
     * 
     * @param token The JWT string.
     * @return The username (email) stored in the token's subject field.
     */
    public String getUsernameFromToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    /**
     * Extracts the user's role claim from the JWT payload.
     * 
     * @param token The JWT string.
     * @return The authorization role name.
     */
    public String getRoleFromToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("role", String.class);
    }

    /**
     * Validates the JWT signature and integrity, checking if the token has expired.
     * 
     * @param token The JWT string.
     * @return true if valid, false if parsing fails (expired, invalid signature, etc).
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            LOGGER.warning("Invalid JWT: " + e.getMessage());
        }
        return false;
    }
}
