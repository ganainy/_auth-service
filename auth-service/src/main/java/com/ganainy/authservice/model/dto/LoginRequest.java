package com.ganainy.authservice.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
<<<<<<< HEAD
=======
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
>>>>>>> c91ebb48e3e199215cab9f2b70c7fb1847f4e963

/**
 * LoginRequest - DTO for user login.
 * 
<<<<<<< HEAD
 * Contains only the email and password needed for authentication.
 * Using a record for immutability and automatic getters.
 */
public record LoginRequest(
        @NotBlank(message = "Email is required") @Email(message = "Email must be a valid email address") String email,

        @NotBlank(message = "Password is required") String password) {
=======
 * Simple DTO with just email and password.
 * Used by: POST /api/v1/auth/login
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;
>>>>>>> c91ebb48e3e199215cab9f2b70c7fb1847f4e963
}
