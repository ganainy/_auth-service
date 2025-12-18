package com.ganainy.authservice.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * UserRegistrationRequest - DTO for new user registration.
 * 
 * =====================================================
 * WHAT IS A DTO (Data Transfer Object)?
 * =====================================================
 * 
 * A DTO is a simple object that carries data between layers.
 * It's specifically designed for the API boundary (Controller layer).
 * 
 * Why not just use the Entity?
 * 
 * 1. SECURITY: Entities contain sensitive data (passwords, internal IDs)
 * that should never be exposed in API responses.
 * 
 * 2. FLEXIBILITY: API structure can differ from database structure.
 * Example: API might combine firstName + lastName into "fullName"
 * 
 * 3. VALIDATION: DTOs have API-specific validation rules.
 * Entity validation is for database constraints.
 * 
 * 4. VERSIONING: You can change DTOs without changing entities.
 * Create v1.UserResponse, v2.UserResponse with different fields.
 * 
 * =====================================================
 * REQUEST vs RESPONSE DTOs
 * =====================================================
 * 
 * Best practice: Separate DTOs for requests and responses!
 * 
 * UserRegistrationRequest → What client SENDS to create a user
 * UserResponse → What server RETURNS about a user
 * 
 * Why separate?
 * - Request has password, response NEVER has password
 * - Response has id, createdAt (server-generated), request doesn't
 * - Different validation rules for each direction
 * 
 * =====================================================
 * VALIDATION ANNOTATIONS
 * =====================================================
 * 
 * These annotations validate the data BEFORE it reaches your service:
 * 
 * @NotBlank - Not null, not empty, not just whitespace
 * @Email - Must look like an email address
 * @Size - String length constraints
 * @NotNull - Cannot be null (but can be empty string)
 * @Min/@Max - Number range constraints
 * @Pattern - Regular expression match
 * 
 *          Validation happens automatically when you use @Valid in Controller:
 *          public User register(@Valid @RequestBody UserRegistrationRequest
 *          request)
 * 
 *          If validation fails, Spring returns 400 Bad Request with error
 *          details.
 */
@Data // Getters, setters, toString, equals, hashCode
@Builder // Builder pattern: UserRegistrationRequest.builder().email("...").build()
@NoArgsConstructor // Empty constructor (needed for JSON deserialization)
@AllArgsConstructor // Constructor with all fields
public class UserRegistrationRequest {

    /**
     * User's email address - will be their login username.
     * 
     * @NotBlank ensures:
     *           - Not null
     *           - Not empty string ""
     *           - Not just whitespace " "
     * 
     * @Email validates the format looks like an email.
     *        Note: @Email is lenient - it just checks for @ symbol and domain.
     *        For stricter validation, use @Email(regexp = "...") with a regex.
     */
    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    private String email;

    /**
     * Plain text password - will be hashed by UserService.
     * 
     * @Size(min=8) enforces minimum length for security.
     * 
     *              In production, you'd also want:
     *              - @Pattern for complexity (uppercase, lowercase, number, symbol)
     *              - Maximum length to prevent DoS attacks (hashing long strings is
     *              slow)
     */
    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    private String password;

    /**
     * User's first name.
     */
    @NotBlank(message = "First name is required")
    @Size(max = 50, message = "First name cannot exceed 50 characters")
    private String firstName;

    /**
     * User's last name.
     */
    @NotBlank(message = "Last name is required")
    @Size(max = 50, message = "Last name cannot exceed 50 characters")
    private String lastName;

    /**
     * Optional: User role (ADMIN, DOCTOR, NURSE, PATIENT, RECEPTIONIST).
     * 
     * If not provided, defaults to PATIENT in UserService.
     * 
     * ⚠️ SECURITY NOTE: In production, regular users should NOT be able
     * to set their own role! Only admins should assign roles.
     * For now, we allow it for learning purposes.
     * We'll secure this properly when we add role-based authorization.
     */
    private String role; // Optional - defaults to PATIENT if not provided
}
