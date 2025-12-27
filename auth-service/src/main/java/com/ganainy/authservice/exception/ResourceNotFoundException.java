package com.ganainy.authservice.exception;

/**
 * ResourceNotFoundException - Thrown when a requested resource doesn't exist.
 * 
 * =====================================================
 * CUSTOM EXCEPTIONS IN SPRING
 * =====================================================
 * 
 * Why create custom exceptions?
 * 
 * 1. SEMANTIC CLARITY: "ResourceNotFoundException" is clearer than
 * "IllegalArgumentException" or a generic "RuntimeException"
 * 
 * 2. SPECIFIC HANDLING: We can catch and handle each exception type
 * differently in @ControllerAdvice
 * 
 * 3. HTTP MAPPING: Each exception can map to a specific HTTP status code
 * - ResourceNotFoundException → 404 Not Found
 * - DuplicateResourceException → 409 Conflict
 * - ValidationException → 400 Bad Request
 * 
 * =====================================================
 * RuntimeException vs Exception
 * =====================================================
 * 
 * We extend RuntimeException (unchecked) instead of Exception (checked):
 * 
 * Checked Exception (extends Exception):
 * - Must be declared in method signature: throws ResourceNotFoundException
 * - Must be caught or declared by every method in the call chain
 * - Good for recoverable errors where caller MUST handle it
 * 
 * Unchecked Exception (extends RuntimeException):
 * - No need to declare in method signature
 * - Can bubble up to @ControllerAdvice automatically
 * - Good for programming errors and business rule violations
 * - Spring's standard approach for web applications
 * 
 * Most modern Spring applications use RuntimeException for simplicity.
 */
public class ResourceNotFoundException extends RuntimeException {

    /**
     * The type of resource that wasn't found (e.g., "User", "Appointment").
     */
    private final String resourceName;

    /**
     * The field used to search (e.g., "id", "email").
     */
    private final String fieldName;

    /**
     * The value that was searched for (e.g., "123", "john@example.com").
     */
    private final Object fieldValue;

    /**
     * Full constructor with all details.
     * 
     * Example usage:
     * throw new ResourceNotFoundException("User", "id", userId);
     * // Message: "User not found with id: 123"
     */
    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s not found with %s: %s", resourceName, fieldName, fieldValue));
        this.resourceName = resourceName;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }

    /**
     * Simple constructor with just a message.
     */
    public ResourceNotFoundException(String message) {
        super(message);
        this.resourceName = null;
        this.fieldName = null;
        this.fieldValue = null;
    }

    // Getters (Lombok @Getter could be used, but keeping explicit for learning)

    public String getResourceName() {
        return resourceName;
    }

    public String getFieldName() {
        return fieldName;
    }

    public Object getFieldValue() {
        return fieldValue;
    }
}
