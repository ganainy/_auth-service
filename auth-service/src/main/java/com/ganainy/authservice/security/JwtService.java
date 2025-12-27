package com.ganainy.authservice.security;

<<<<<<< HEAD
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
=======
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Base64;
>>>>>>> c91ebb48e3e199215cab9f2b70c7fb1847f4e963
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * JwtService - Utility class for JWT token operations.
 * 
 * =====================================================
<<<<<<< HEAD
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
=======
 * WHAT THIS CLASS DOES
 * =====================================================
 * 
 * This service handles all JWT-related operations:
 * 1. GENERATE tokens (after successful login)
 * 2. VALIDATE tokens (on every request)
 * 3. EXTRACT data from tokens (user email, roles, etc.)
 * 
 * =====================================================
 * JWT STRUCTURE REMINDER
 * =====================================================
 * 
 * A JWT consists of 3 parts, separated by dots:
 * 
 * eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huQGhvc3BpdGFsLmNvbSJ9.signature
 * |____HEADER_____|.|____________PAYLOAD_____________|.|_SIGNATURE_|
 * 
 * HEADER (JSON, Base64-encoded):
 * {"alg": "HS256", "typ": "JWT"}
 * 
 * PAYLOAD (JSON, Base64-encoded) - called "Claims":
 * {
 * "sub": "john@hospital.com", // Subject (who the token is for)
 * "iat": 1702947200, // Issued At (when created)
 * "exp": 1702950800, // Expiration (when it expires)
 * "role": "DOCTOR" // Custom claims (anything you want)
 * }
 * 
 * SIGNATURE:
 * HMAC-SHA256(base64(header) + "." + base64(payload), SECRET_KEY)
 * - Proves the token wasn't tampered with
 * - Only the server knows the SECRET_KEY
 * 
 * =====================================================
 * 
 * @Value - Injecting Configuration Properties
 *        =====================================================
 * 
 *        @Value("${jwt.secret}") reads the value from application.properties.
 * 
 *        It's like Spring saying:
 *        "Go find 'jwt.secret' in the properties file and inject it here."
 * 
 *        This is how we externalize configuration - the code doesn't contain
 *        hardcoded values, making it easy to change between environments
 *        (dev, staging, production).
 */
@Service
>>>>>>> c91ebb48e3e199215cab9f2b70c7fb1847f4e963
@Slf4j
public class JwtService {

    /**
<<<<<<< HEAD
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
=======
     * Secret key for signing tokens.
     * Injected from application.properties: jwt.secret
     */
    @Value("${jwt.secret}")
    private String secretKey;

    /**
     * Token expiration time in milliseconds.
     * Injected from application.properties: jwt.expiration
     * Default: 1 hour (3600000ms)
     */
    @Value("${jwt.expiration}")
    private long jwtExpiration;

    // =====================================================
    // TOKEN GENERATION
    // =====================================================

    /**
     * Generate a JWT token for a user.
     * 
     * This is called after successful login.
     * 
     * @param userDetails The authenticated user (from Spring Security)
     * @return A signed JWT token string
>>>>>>> c91ebb48e3e199215cab9f2b70c7fb1847f4e963
     */
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    /**
<<<<<<< HEAD
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
=======
     * Generate a JWT token with extra claims.
     * 
     * Use this to add custom data to the token (role, permissions, etc.)
     * 
     * @param extraClaims Additional data to include in the token
     * @param userDetails The authenticated user
     * @return A signed JWT token string
     * 
     *         Example:
     *         Map<String, Object> claims = new HashMap<>();
     *         claims.put("role", "DOCTOR");
     *         claims.put("department", "Cardiology");
     *         String token = jwtService.generateToken(claims, userDetails);
     */
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return buildToken(extraClaims, userDetails, jwtExpiration);
    }

    /**
     * Builds the actual JWT token.
     * 
     * Uses the JJWT library's builder pattern to construct the token:
     * 1. Set custom claims (extra data)
     * 2. Set the subject (username/email)
     * 3. Set issued-at timestamp
     * 4. Set expiration timestamp
     * 5. Sign with our secret key
     * 6. Convert to compact string format
     */
    private String buildToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails,
            long expiration) {

        return Jwts.builder()
                // Add custom claims first (they can be overwritten by standard claims)
                .claims(extraClaims)
                // Standard claims
                .subject(userDetails.getUsername()) // Who this token is for
                .issuedAt(new Date(System.currentTimeMillis())) // When issued
                .expiration(new Date(System.currentTimeMillis() + expiration)) // When expires
                // Sign the token
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                // Build to compact string format
                .compact();
    }

    // =====================================================
    // TOKEN VALIDATION
    // =====================================================

    /**
     * Validate a JWT token.
     * 
     * A token is valid if:
     * 1. It can be parsed (not corrupted)
     * 2. The signature is valid (not tampered with)
     * 3. It hasn't expired
     * 4. The subject matches the provided UserDetails
     * 
     * @param token       The JWT token to validate
     * @param userDetails The user to validate against
     * @return true if token is valid, false otherwise
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
        } catch (Exception e) {
            log.warn("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Check if a token is valid (without checking against a specific user).
     * 
     * This performs basic validation:
     * 1. Can be parsed
     * 2. Signature is valid
     * 3. Not expired
     */
    public boolean isTokenValid(String token) {
        try {
            extractAllClaims(token);
            return !isTokenExpired(token);
        } catch (ExpiredJwtException e) {
            log.warn("Token expired: {}", e.getMessage());
            return false;
        } catch (SignatureException e) {
            log.warn("Invalid token signature: {}", e.getMessage());
            return false;
        } catch (MalformedJwtException e) {
            log.warn("Malformed token: {}", e.getMessage());
            return false;
        } catch (UnsupportedJwtException e) {
            log.warn("Unsupported token: {}", e.getMessage());
            return false;
        } catch (IllegalArgumentException e) {
            log.warn("Token claims string is empty: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Check if a token has expired.
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // =====================================================
    // CLAIM EXTRACTION
    // =====================================================

    /**
     * Extract the username (subject) from a token.
     * 
     * The "subject" claim typically contains the user identifier (email).
>>>>>>> c91ebb48e3e199215cab9f2b70c7fb1847f4e963
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
<<<<<<< HEAD
     * Extract the expiration date from the token.
=======
     * Extract the expiration date from a token.
>>>>>>> c91ebb48e3e199215cab9f2b70c7fb1847f4e963
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
<<<<<<< HEAD
     * Generic method to extract any claim from the token.
     * 
     * Uses a function to specify which claim to extract.
     * Example: extractClaim(token, Claims::getSubject)
=======
     * Extract a specific claim from a token.
     * 
     * This is a generic method that works with any claim.
     * Uses a Function to specify which claim to extract.
     * 
     * Example:
     * String email = extractClaim(token, Claims::getSubject);
     * Date expiry = extractClaim(token, Claims::getExpiration);
     * String role = extractClaim(token, claims -> claims.get("role",
     * String.class));
     * 
     * @param token          The JWT token
     * @param claimsResolver Function to extract the desired claim
     * @return The extracted claim value
>>>>>>> c91ebb48e3e199215cab9f2b70c7fb1847f4e963
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
<<<<<<< HEAD
     * Parse the token and extract all claims.
     * 
     * This method:
     * 1. Verifies the token signature (ensures it wasn't tampered with)
     * 2. Checks the token isn't expired
     * 3. Parses the payload into Claims object
     * 
     * If anything is wrong (invalid signature, expired, malformed),
=======
     * Extract all claims from a token.
     * 
     * This parses the token and verifies the signature.
     * If the signature is invalid or the token is expired,
>>>>>>> c91ebb48e3e199215cab9f2b70c7fb1847f4e963
     * an exception is thrown.
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
<<<<<<< HEAD
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
=======
                .verifyWith(getSigningKey()) // Use our secret to verify signature
                .build()
                .parseSignedClaims(token) // Parse and verify
                .getPayload(); // Get the claims (payload)
    }

    // =====================================================
    // HELPER METHODS
    // =====================================================

    /**
     * Get the signing key from the secret.
     * 
     * The secret is stored as a Base64-encoded string.
     * We decode it and create an HMAC-SHA key from it.
     * 
     * Why HMAC-SHA256?
     * - It's a symmetric algorithm (same key to sign and verify)
     * - Fast and secure for most applications
     * - Requires the key to be at least 256 bits (32 bytes)
     * 
     * Alternative: RSA (asymmetric) - useful when tokens are verified
     * by different services that shouldn't have the private key.
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = Base64.getDecoder().decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Get the token expiration time in milliseconds.
     */
    public long getExpirationTime() {
        return jwtExpiration;
>>>>>>> c91ebb48e3e199215cab9f2b70c7fb1847f4e963
    }
}
