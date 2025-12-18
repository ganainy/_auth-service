package com.ganainy.authservice.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * GlobalExceptionHandler - Centralized exception handling for all controllers.
 * 
 * =====================================================
 * WHAT IS @RestControllerAdvice?
 * =====================================================
 * 
 * @RestControllerAdvice = @ControllerAdvice + @ResponseBody
 * 
 *                       It creates a GLOBAL exception handler that:
 *                       1. Intercepts exceptions from ALL controllers
 *                       2. Converts exceptions to HTTP responses
 *                       3. Applies to the entire application (not just one
 *                       controller)
 * 
 *                       Think of it as a "catch-all" for your REST API.
 * 
 *                       Without @ControllerAdvice:
 *                       - Each controller handles its own exceptions
 *                       - Duplicate code everywhere
 *                       - Inconsistent error formats
 * 
 *                       With @ControllerAdvice:
 *                       - One place to handle all exceptions
 *                       - DRY (Don't Repeat Yourself)
 *                       - Consistent error responses
 * 
 *                       =====================================================
 *                       HOW IT WORKS
 *                       =====================================================
 * 
 *                       1. Controller method throws exception
 *                       2. Spring looks for @ExceptionHandler that matches
 *                       3. If found here, this handler processes it
 *                       4. Handler returns ResponseEntity with error details
 *                       5. Client receives clean JSON error response
 * 
 *                       Request Flow:
 *                       Client → Controller → throws Exception
 *                       ↓
 *                       GlobalExceptionHandler
 *                       ↓
 *                       Client ← JSON Error Response (400, 404, 500, etc.)
 * 
 *                       =====================================================
 * @ExceptionHandler
 *                   =====================================================
 * 
 *                   Marks a method as an exception handler for specific
 *                   exception type(s).
 * 
 * @ExceptionHandler(ResourceNotFoundException.class)
 *                                                    public
 *                                                    ResponseEntity<ApiError>
 *                                                    handleNotFound(ResourceNotFoundException
 *                                                    ex) {
 *                                                    // Handle
 *                                                    ResourceNotFoundException
 *                                                    return
 *                                                    ResponseEntity.status(404).body(errorResponse);
 *                                                    }
 * 
 *                                                    You can handle multiple
 *                                                    exceptions:
 * @ExceptionHandler({IOException.class, SQLException.class})
 * 
 *                                       Spring matches the MOST SPECIFIC
 *                                       handler:
 *                                       - ResourceNotFoundException →
 *                                       handleResourceNotFound
 *                                       - IllegalArgumentException →
 *                                       handleIllegalArgument
 *                                       - Any other Exception →
 *                                       handleAllExceptions (fallback)
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    private static final String ERROR_TYPE_BASE = "https://api.healthcare.com/errors/";

    // =====================================================
    // 404 NOT FOUND - Resource doesn't exist
    // =====================================================

    /**
     * Handles ResourceNotFoundException.
     * 
     * Triggered when: User, appointment, or other resource not found by ID/email
     * HTTP Status: 404 Not Found
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleResourceNotFound(
            ResourceNotFoundException ex,
            HttpServletRequest request) {

        log.warn("Resource not found: {} at path: {}", ex.getMessage(), request.getRequestURI());

        ApiError error = ApiError.builder()
                .type(ERROR_TYPE_BASE + "resource-not-found")
                .title("Resource Not Found")
                .status(HttpStatus.NOT_FOUND.value())
                .detail(ex.getMessage())
                .instance(request.getRequestURI())
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    // =====================================================
    // 409 CONFLICT - Duplicate resource
    // =====================================================

    /**
     * Handles DuplicateResourceException.
     * 
     * Triggered when: Creating a resource that already exists (e.g., duplicate
     * email)
     * HTTP Status: 409 Conflict
     * 
     * Why 409 and not 400?
     * - 400 = bad request format/syntax
     * - 409 = request is valid, but conflicts with existing state
     */
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ApiError> handleDuplicateResource(
            DuplicateResourceException ex,
            HttpServletRequest request) {

        log.warn("Duplicate resource: {} at path: {}", ex.getMessage(), request.getRequestURI());

        ApiError error = ApiError.builder()
                .type(ERROR_TYPE_BASE + "duplicate-resource")
                .title("Resource Already Exists")
                .status(HttpStatus.CONFLICT.value())
                .detail(ex.getMessage())
                .instance(request.getRequestURI())
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    // =====================================================
    // 401 UNAUTHORIZED - Invalid credentials
    // =====================================================

    /**
     * Handles InvalidCredentialsException.
     * 
     * Triggered when: Wrong password, invalid token, etc.
     * HTTP Status: 401 Unauthorized
     * 
     * Security Note: Don't reveal whether email exists or password is wrong.
     * Generic "Invalid credentials" is safer.
     */
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiError> handleInvalidCredentials(
            InvalidCredentialsException ex,
            HttpServletRequest request) {

        log.warn("Invalid credentials attempt at path: {}", request.getRequestURI());

        ApiError error = ApiError.builder()
                .type(ERROR_TYPE_BASE + "invalid-credentials")
                .title("Invalid Credentials")
                .status(HttpStatus.UNAUTHORIZED.value())
                .detail(ex.getMessage())
                .instance(request.getRequestURI())
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    // =====================================================
    // 400 BAD REQUEST - Validation failures
    // =====================================================

    /**
     * Handles validation errors from @Valid annotation.
     * 
     * Triggered when: DTO validation fails (e.g., @NotBlank, @Email, @Size)
     * HTTP Status: 400 Bad Request
     * 
     * =====================================================
     * MethodArgumentNotValidException
     * =====================================================
     * 
     * This exception is thrown by Spring when @Valid validation fails.
     * It contains a BindingResult with all field errors.
     * 
     * Example trigger:
     * 
     * @PostMapping
     *              public User create(@Valid @RequestBody UserRegistrationRequest
     *              request)
     *              // If request.email is empty, MethodArgumentNotValidException is
     *              thrown
     * 
     *              We extract each field error and format it nicely.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidationErrors(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        BindingResult bindingResult = ex.getBindingResult();

        // Extract all field errors
        List<ApiError.FieldError> fieldErrors = bindingResult.getFieldErrors().stream()
                .map(error -> ApiError.FieldError.builder()
                        .field(error.getField())
                        .message(error.getDefaultMessage())
                        .rejectedValue(error.getRejectedValue())
                        .build())
                .collect(Collectors.toList());

        log.warn("Validation failed for {} fields at path: {}",
                fieldErrors.size(), request.getRequestURI());

        ApiError error = ApiError.builder()
                .type(ERROR_TYPE_BASE + "validation-failed")
                .title("Validation Failed")
                .status(HttpStatus.BAD_REQUEST.value())
                .detail("One or more fields have validation errors")
                .instance(request.getRequestURI())
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .errors(fieldErrors)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Handles type conversion errors (e.g., string provided instead of number).
     * 
     * Triggered when: Path variable or query param can't be converted
     * Example: GET /api/v1/users/abc (abc can't be converted to Long)
     * HTTP Status: 400 Bad Request
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex,
            HttpServletRequest request) {

        String message = String.format("Parameter '%s' should be of type '%s' but was '%s'",
                ex.getName(),
                ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown",
                ex.getValue());

        log.warn("Type mismatch: {} at path: {}", message, request.getRequestURI());

        ApiError error = ApiError.builder()
                .type(ERROR_TYPE_BASE + "type-mismatch")
                .title("Invalid Parameter Type")
                .status(HttpStatus.BAD_REQUEST.value())
                .detail(message)
                .instance(request.getRequestURI())
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    // =====================================================
    // 400 BAD REQUEST - IllegalArgumentException
    // =====================================================

    /**
     * Handles IllegalArgumentException (business rule violations).
     * 
     * Triggered when: Service layer rejects input (e.g., invalid role, bad data)
     * HTTP Status: 400 Bad Request
     * 
     * Note: We're still using IllegalArgumentException in our service.
     * Later, we'll replace these with more specific custom exceptions.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgument(
            IllegalArgumentException ex,
            HttpServletRequest request) {

        log.warn("Illegal argument: {} at path: {}", ex.getMessage(), request.getRequestURI());

        // Check if it's a duplicate email error (temporary until we update service)
        HttpStatus status = HttpStatus.BAD_REQUEST;
        String type = "bad-request";
        String title = "Bad Request";

        if (ex.getMessage() != null && ex.getMessage().contains("already exists")) {
            status = HttpStatus.CONFLICT;
            type = "duplicate-resource";
            title = "Resource Already Exists";
        } else if (ex.getMessage() != null && ex.getMessage().contains("not found")) {
            status = HttpStatus.NOT_FOUND;
            type = "resource-not-found";
            title = "Resource Not Found";
        }

        ApiError error = ApiError.builder()
                .type(ERROR_TYPE_BASE + type)
                .title(title)
                .status(status.value())
                .detail(ex.getMessage())
                .instance(request.getRequestURI())
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(status).body(error);
    }

    // =====================================================
    // 500 INTERNAL SERVER ERROR - Fallback for unhandled exceptions
    // =====================================================

    /**
     * Fallback handler for any unhandled exceptions.
     * 
     * This is the "catch-all" that ensures we NEVER return a raw stack trace.
     * 
     * HTTP Status: 500 Internal Server Error
     * 
     * SECURITY: Never expose internal details to clients!
     * We log the full stack trace for debugging, but return a generic message.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleAllExceptions(
            Exception ex,
            HttpServletRequest request) {

        // Log the FULL exception for debugging (only visible in server logs)
        log.error("Unhandled exception at path: {}", request.getRequestURI(), ex);

        // Return GENERIC message to client (no internal details!)
        ApiError error = ApiError.builder()
                .type(ERROR_TYPE_BASE + "internal-error")
                .title("Internal Server Error")
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .detail("An unexpected error occurred. Please try again later.")
                .instance(request.getRequestURI())
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
