package com.ganainy.authservice.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * CreateUserRequest - DTO for creating a new user.
 * 
 * =====================================================
 * WHY USE A DTO INSTEAD OF THE ENTITY DIRECTLY?
 * =====================================================
 * 
 * DTOs (Data Transfer Objects) are best practice for several reasons:
 * 
 * 1. **Security**: Clients can't set fields they shouldn't (like 'id', 'role',
 * 'enabled')
 * 2. **Validation**: Different operations can have different validation rules
 * 3. **Decoupling**: API contract is separate from database schema
 * 4. **Flexibility**: Request format can differ from entity structure
 * 
 * Example problem without DTOs:
 * - Client sends: {"id": 999, "role": "ADMIN", "enabled": true, ...}
 * - If we bind directly to User entity, they could make themselves an admin!
 * 
 * With DTOs:
 * - CreateUserRequest only has: email, password, firstName, lastName
 * - Server sets role, enabled, etc. with safe defaults
 * 
 * =====================================================
 * JAVA RECORDS (Java 14+)
 * =====================================================
 * 
 * A record is a compact way to create immutable data classes.
 * Java automatically generates:
 * - Constructor with all fields
 * - Getters (email(), password(), etc.)
 * - equals() and hashCode()
 * - toString()
 * 
 * Perfect for DTOs!
 */
public record CreateUserRequest(
        @NotBlank(message = "Email is required") @Email(message = "Email must be a valid email address") String email,

        @NotBlank(message = "Password is required") @Size(min = 8, message = "Password must be at least 8 characters") String password,

        @NotBlank(message = "First name is required") @Size(max = 50, message = "First name cannot exceed 50 characters") String firstName,

        @NotBlank(message = "Last name is required") @Size(max = 50, message = "Last name cannot exceed 50 characters") String lastName) {
}
