package com.ganainy.authservice.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * LoginRequest - DTO for user login.
 * 
 * Contains only the email and password needed for authentication.
 * Using a record for immutability and automatic getters.
 */
public record LoginRequest(
        @NotBlank(message = "Email is required") @Email(message = "Email must be a valid email address") String email,

        @NotBlank(message = "Password is required") String password) {
}
