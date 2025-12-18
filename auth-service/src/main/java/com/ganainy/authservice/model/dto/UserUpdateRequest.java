package com.ganainy.authservice.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * UserUpdateRequest - DTO for updating user profile.
 * 
 * =====================================================
 * DIFFERENCE FROM REGISTRATION REQUEST
 * =====================================================
 * 
 * UserRegistrationRequest: All fields required, includes password
 * UserUpdateRequest: Only updatable fields, NO password
 * 
 * Password changes should go through a separate, more secure endpoint
 * that requires the current password for verification.
 * 
 * =====================================================
 * VALIDATION STRATEGY
 * =====================================================
 * 
 * For updates, you often want to allow PARTIAL updates:
 * - Only update the fields that were provided
 * - Leave other fields unchanged
 * 
 * To enable this, we use @NotBlank on required fields but allow
 * the service layer to handle null checks for optional updates.
 * 
 * More advanced: Use @NotBlank only for PATCH requests, not PUT.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequest {

    /**
     * New email address (optional for update).
     * 
     * If provided, must be valid format.
     * Service layer will check if it's already taken.
     */
    @Email(message = "Please provide a valid email address")
    private String email;

    /**
     * New first name.
     */
    @Size(max = 50, message = "First name cannot exceed 50 characters")
    private String firstName;

    /**
     * New last name.
     */
    @Size(max = 50, message = "Last name cannot exceed 50 characters")
    private String lastName;
}
