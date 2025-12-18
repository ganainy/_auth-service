package com.ganainy.authservice.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * ApiError - Standardized error response format.
 * 
 * =====================================================
 * RFC 7807 - Problem Details for HTTP APIs
 * =====================================================
 * 
 * This follows RFC 7807, a standard format for error responses:
 * https://datatracker.ietf.org/doc/html/rfc7807
 * 
 * Standard fields:
 * - type: URI identifying the error type (can be a docs link)
 * - title: Short, human-readable summary
 * - status: HTTP status code
 * - detail: Detailed human-readable explanation
 * - instance: URI of the specific request that caused the error
 * 
 * We also add:
 * - timestamp: When the error occurred
 * - errors: List of specific validation errors (for 400 responses)
 * - path: Simplified request path
 * 
 * =====================================================
 * WHY STANDARDIZE ERROR RESPONSES?
 * =====================================================
 * 
 * 1. CONSISTENCY: All errors look the same, easy for clients to parse
 * 2. DEBUGGING: Clear information helps developers fix issues
 * 3. SECURITY: Controlled output prevents leaking sensitive info
 * 4. DOCUMENTATION: Standard format is self-documenting
 * 
 * Example response:
 * 
 * {
 * "type": "https://api.healthcare.com/errors/duplicate-email",
 * "title": "Email Already Exists",
 * "status": 409,
 * "detail": "The email 'john@hospital.com' is already registered.",
 * "instance": "/api/v1/users",
 * "timestamp": "2024-01-15T10:30:00",
 * "path": "/api/v1/users"
 * }
 * 
 * =====================================================
 * @JsonInclude(NON_NULL)
 * =====================================================
 * 
 * This Jackson annotation excludes null fields from JSON output.
 * 
 * Without it: {"type": "...", "errors": null, "fieldErrors": null}
 * With it: {"type": "...",} // null fields are hidden
 * 
 * Keeps responses clean - only shows relevant fields.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL) // Don't serialize null fields
public class ApiError {

    /**
     * URI reference identifying the error type.
     * Should point to documentation explaining this error.
     * Example: "https://api.healthcare.com/errors/validation-failed"
     */
    private String type;

    /**
     * Short, human-readable summary of the problem.
     * Should be the same for all instances of this error type.
     * Example: "Validation Failed"
     */
    private String title;

    /**
     * HTTP status code.
     * Example: 400, 404, 409, 500
     */
    private int status;

    /**
     * Detailed, human-readable explanation specific to this occurrence.
     * Example: "The email 'john@hospital.com' is already registered."
     */
    private String detail;

    /**
     * URI reference to the specific request instance.
     * Usually the request path.
     * Example: "/api/v1/users"
     */
    private String instance;

    /**
     * When the error occurred.
     */
    private LocalDateTime timestamp;

    /**
     * Request path (simpler alternative to instance).
     */
    private String path;

    /**
     * List of validation errors for 400 Bad Request.
     * Each error contains field name and message.
     * 
     * Example:
     * [
     * {"field": "email", "message": "must be a valid email address"},
     * {"field": "password", "message": "must be at least 8 characters"}
     * ]
     */
    private List<FieldError> errors;

    /**
     * Additional error details as key-value pairs.
     * Useful for complex validation scenarios.
     */
    private Map<String, Object> additionalInfo;

    /**
     * Nested class for field-level validation errors.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FieldError {
        private String field;
        private String message;
        private Object rejectedValue;
    }
}
