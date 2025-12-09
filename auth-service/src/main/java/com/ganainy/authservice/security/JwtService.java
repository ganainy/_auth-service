package com.ganainy.authservice.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * JwtService - Utility class for JWT token operations.
 * 
 * =====================================================
 * WHAT IS A JWT (JSON Web Token)?
 * =====================================================
 * 
 * A JWT is a compact, URL-safe token that contains claims (data) about a user.
 * It consists of three parts separated by dots:
 * 
 * eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huQGV4YW1wbGUuY29tIn0.signature
 * └────── Header ──────┘.└────────── Payload ──────────────┘.└─ Sign ─┘
 * 
 * 1. HEADER: Algorithm and token type
 * { "alg": "HS256", "typ": "JWT" }
 * 
 * 2. PAYLOAD: Claims (user data)
 * { "sub": "john@example.com", "iat": 1234567890, "exp": 1234571490 }
 * 
 * 3. SIGNATURE: Ensures the token wasn't tampered with
 * HMACSHA256(base64(header) + "." + base64(payload), secret)
 * 
 * =====================================================
 * WHY JWT?
 * =====================================================
 * 
 * 1. STATELESS: Server doesn't need to store session data
 * 2. SELF-CONTAINED: Token contains all user info needed
 * 3. SCALABLE: Any server can validate the token
 * 4. EXPIRABLE: Tokens automatically expire
 * 
 * Flow:
 * 1. User logs in with username/password
 * 2. Server validates credentials and returns JWT
 * 3. Client stores JWT (localStorage, cookie, etc.)
 * 4. Client sends JWT in Authorization header for each request
 * 5. Server validates JWT and extracts user info
 * 
 * =====================================================
 * SECURITY CONSIDERATIONS
 * =====================================================
 * 
 * ⚠️ The secret key MUST be:
 * - At least 256 bits (32 bytes) for HS256
 * - Kept secret (never commit to git!)
 * - Different for each environment (dev, prod)
 * 
 * ⚠️ Token expiration should be short:
 * - Access tokens: 15 minutes to 1 hour
 * - Refresh tokens: 1 week to 1 month
 * 
 * In production, use environment variables for the secret!
 */
@Component
@Slf4j
public class JwtService {

    /**
     * JWT_SECRET: The secret key used to sign and verify tokens.
     * 
     * @Value injects the value from application.properties.
     *        If not found, uses the default value (for development only!).
     * 
     *        ⚠️ IMPORTANT: In production, NEVER use this default value!
     *        Set a secure random key in your environment variables or config
     *        server.
     * 
     *        The secret must be at least 256 bits (32 characters) for HS256.
     */
    @Value("${jwt.secret:mySecretKeyForJWTTokenGenerationThatIsAtLeast256BitsLong}")
    private String jwtSecret;

    /**
     * JWT_EXPIRATION: How long the token is valid (in milliseconds).
     * 
     * Default: 24 hours (86400000 ms)
     * For production, consider shorter expiration (15 min - 1 hour)
     * combined with refresh tokens.
     */
    @Value("${jwt.expiration:86400000}")
    private long jwtExpiration;

    /**
     * Get the signing key from the secret string.
     * 
     * The key is used for both signing (when creating tokens)
     * and verifying (when validating tokens).
     */
    private SecretKey getSigningKey() {
        // Convert the secret string to a SecretKey
        // Keys.hmacShaKeyFor() ensures the key is suitable for HMAC-SHA algorithms
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    /**
     * =====================================================
     * GENERATE TOKEN
     * =====================================================
     * 
     * Creates a new JWT for the given user.
     * 
     * @param userDetails The authenticated user's details
     * @return A signed JWT string
     */
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    /**
     * Generate a token with extra claims.
     * 
     * Claims are key-value pairs stored in the token payload.
     * Common claims:
     * - sub (subject): Usually the username or user ID
     * - iat (issued at): When the token was created
     * - exp (expiration): When the token expires
     * - roles: User's roles for authorization
     * 
     * @param extraClaims Additional claims to include in the token
     * @param userDetails The authenticated user's details
     * @return A signed JWT string
     */
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return Jwts.builder()
                // Add any extra claims (role, permissions, etc.)
                .claims(extraClaims)
                // Subject is typically the username (unique identifier)
                .subject(userDetails.getUsername())
                // When the token was issued
                .issuedAt(new Date(System.currentTimeMillis()))
                // When the token expires
                .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
                // Sign the token with our secret key using HS256 algorithm
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                // Build the token string
                .compact();
    }

    /**
     * =====================================================
     * EXTRACT USERNAME
     * =====================================================
     * 
     * Extracts the username (subject claim) from the token.
     * This is used to look up the user in the database.
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extract the expiration date from the token.
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Generic method to extract any claim from the token.
     * 
     * Uses a function to specify which claim to extract.
     * Example: extractClaim(token, Claims::getSubject)
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Parse the token and extract all claims.
     * 
     * This method:
     * 1. Verifies the token signature (ensures it wasn't tampered with)
     * 2. Checks the token isn't expired
     * 3. Parses the payload into Claims object
     * 
     * If anything is wrong (invalid signature, expired, malformed),
     * an exception is thrown.
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                // Set the key to verify the signature
                .verifyWith(getSigningKey())
                // Build the parser
                .build()
                // Parse the token and get the claims
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * =====================================================
     * VALIDATE TOKEN
     * =====================================================
     * 
     * Checks if the token is valid for the given user.
     * 
     * A token is valid if:
     * 1. The signature is correct (verified by extractAllClaims)
     * 2. The username in the token matches the UserDetails
     * 3. The token hasn't expired
     * 
     * @param token       The JWT to validate
     * @param userDetails The user to validate against
     * @return true if valid, false otherwise
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
        } catch (JwtException | IllegalArgumentException e) {
            // Token is invalid (malformed, expired, wrong signature, etc.)
            log.warn("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Check if the token has expired.
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
}
