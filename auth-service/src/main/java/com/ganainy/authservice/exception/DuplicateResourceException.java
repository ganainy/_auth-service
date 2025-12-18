package com.ganainy.authservice.exception;

/**
 * DuplicateResourceException - Thrown when creating a resource that already
 * exists.
 * 
 * Maps to HTTP 409 Conflict.
 * 
 * Examples:
 * - Registering with an email that's already taken
 * - Creating a user with a duplicate username
 * - Scheduling an appointment at an already-booked slot
 */
public class DuplicateResourceException extends RuntimeException {

    private final String resourceName;
    private final String fieldName;
    private final Object fieldValue;

    public DuplicateResourceException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s already exists with %s: %s", resourceName, fieldName, fieldValue));
        this.resourceName = resourceName;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }

    public DuplicateResourceException(String message) {
        super(message);
        this.resourceName = null;
        this.fieldName = null;
        this.fieldValue = null;
    }

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
