package com.ganainy.authservice.model.dto;

import com.ganainy.authservice.model.entity.User;

import java.time.LocalDateTime;

/**
 * UserResponse - DTO for returning user data to clients.
 * 
 * =====================================================
 * WHY A SEPARATE RESPONSE DTO?
 * =====================================================
 * 
 * We NEVER want to send the password hash to clients!
 * Even though it's hashed, exposing it is a security risk.
 * 
 * The response DTO:
 * - Excludes sensitive fields (password)
 * - Can include computed fields
 * - Decouples API response from database schema
 */
public record UserResponse(
        Long id,
        String email,
        String firstName,
        String lastName,
        User.Role role,
        boolean enabled,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
    /**
     * Factory method to convert a User entity to a UserResponse DTO.
     * 
     * This is a simple mapping approach. For complex mappings,
     * you'd use a library like MapStruct.
     */
    public static UserResponse fromEntity(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole(),
                user.isEnabled(),
                user.getCreatedAt(),
                user.getUpdatedAt());
    }
}
