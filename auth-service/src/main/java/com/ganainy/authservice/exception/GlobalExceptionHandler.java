package com.ganainy.authservice.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * GlobalExceptionHandler - Centralized exception handling for all controllers.
 * 
 * =====================================================
 * WHAT IS @RestControllerAdvice?
 * =====================================================
 * 
 * @RestControllerAdvice is a specialization of @ControllerAdvice that:
 *                       1. Applies to ALL controllers in the application
 *                       2. Automatically adds @ResponseBody to all methods
 *                       3. Catches exceptions thrown by any controller
 * 
 *                       Without this, each controller would need its own
 *                       try-catch blocks,
 *                       leading to duplicated error handling code.
 * 
 *                       With @RestControllerAdvice, we define exception
 *                       handlers ONCE and
 *                       they apply everywhere!
 * 
 *                       How it works:
 * 
 *                       Controller throws Exception
 *                       ↓
 *                       Spring looks for @ExceptionHandler
 *                       in @RestControllerAdvice
 *                       ↓
 *                       Matching handler method is called
 *                       ↓
 *                       Handler returns error response to client
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Handle validation errors from @Valid annotations.
     * 
     * When a request body fails validation (e.g., @NotBlank, @Email, @Size),
     * Spring throws MethodArgumentNotValidException.
     * 
     * We catch it here and return a user-friendly error response.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex) {
        log.warn("Validation failed: {}", ex.getMessage());

        // Extract all field errors
        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        }

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Validation Failed",
                "One or more fields have validation errors",
                LocalDateTime.now(),
                fieldErrors);

        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Handle IllegalArgumentException (e.g., duplicate email).
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Illegal argument: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                ex.getMessage(),
                LocalDateTime.now(),
                null);

        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Handle RuntimeException (e.g., user not found).
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException ex) {
        log.error("Runtime exception: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                ex.getMessage(),
                LocalDateTime.now(),
                null);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    /**
     * Error response structure - consistent format for all errors.
     */
    public record ErrorResponse(
            int status,
            String error,
            String message,
            LocalDateTime timestamp,
            Map<String, String> fieldErrors) {
    }
}
