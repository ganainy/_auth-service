package com.ganainy.authservice.controller;

import com.ganainy.authservice.mapper.UserMapper;
import com.ganainy.authservice.model.dto.UserRegistrationRequest;
import com.ganainy.authservice.model.dto.UserResponse;
import com.ganainy.authservice.model.dto.UserUpdateRequest;
import com.ganainy.authservice.model.entity.User;
import com.ganainy.authservice.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * UserController - REST API endpoints for User management.
 * 
 * =====================================================
 * WHAT IS A REST CONTROLLER?
 * =====================================================
 * 
 * A REST Controller handles HTTP requests and returns data (usually JSON).
 * It's the "front door" of your application - where HTTP meets Java.
 * 
 * Request Flow:
 * Client → HTTP Request → Controller → Service → Repository → Database
 * ↓
 * Client ← HTTP Response ← Controller ← Service ← Repository ← Database
 * 
 * Controller responsibilities:
 * ✅ Parse HTTP requests (URL, headers, body)
 * ✅ Validate input data
 * ✅ Call the appropriate service method
 * ✅ Convert results to HTTP responses
 * ✅ Handle HTTP status codes (200, 201, 404, etc.)
 * 
 * Controller does NOT:
 * ❌ Contain business logic (that's the Service's job)
 * ❌ Access the database directly (that's the Repository's job)
 * ❌ Know about SQL or JPA (abstracted by lower layers)
 * 
 * =====================================================
 * 
 * @RestController
 *                 =====================================================
 * 
 * @RestController = @Controller + @ResponseBody
 * 
 * @Controller: Marks this as a Spring MVC controller
 * @ResponseBody: Return values are serialized to JSON (not view names)
 * 
 *                Without @ResponseBody, return "home" would look for home.html
 *                template.
 *                With @ResponseBody, return "home" would send "home" as JSON
 *                text.
 * 
 * @RestController is perfect for REST APIs that return JSON.
 * 
 *                 =====================================================
 *                 @RequestMapping("/api/v1/users")
 *                 =====================================================
 * 
 *                 Sets the BASE PATH for all endpoints in this controller.
 * 
 *                 Result:
 * @GetMapping → GET /api/v1/users
 *             @GetMapping("/{id}") → GET /api/v1/users/123
 * @PostMapping → POST /api/v1/users
 * 
 *              "/api/v1" is API versioning - makes it easy to create v2 later
 *              without breaking existing clients.
 */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;

    // =====================================================
    // CREATE - POST /api/v1/users
    // =====================================================

    /**
     * Register a new user.
     * 
     * HTTP: POST /api/v1/users
     * Request Body: UserRegistrationRequest (JSON)
     * Response: UserResponse (JSON) with status 201 Created
     * 
     * =====================================================
     * ANNOTATIONS EXPLAINED
     * =====================================================
     * 
     * @PostMapping
     *              - Handles HTTP POST requests
     *              - POST is used for CREATING new resources
     *              - Shorthand for @RequestMapping(method = RequestMethod.POST)
     * 
     * @Valid
     *        - Triggers validation on the request body
     *        - Checks all @NotBlank, @Email, @Size annotations in the DTO
     *        - If validation fails, Spring returns 400 Bad Request automatically
     *        - We'll customize the error response with @ControllerAdvice later
     * 
     * @RequestBody
     *              - Tells Spring to deserialize the HTTP request body into this
     *              object
     *              - JSON → Java object conversion using Jackson library
     *              - Required for POST, PUT, PATCH requests with body data
     * 
     *              ResponseEntity<T>
     *              - Wrapper that includes response body + HTTP status + headers
     *              - Gives full control over the HTTP response
     *              - ResponseEntity.status(201).body(data) sets status and body
     * 
     *              =====================================================
     *              HTTP STATUS CODES
     *              =====================================================
     * 
     *              201 Created - New resource was successfully created
     *              - Standard response for successful POST that creates something
     *              - Should include the created resource in the body
     *              - Optionally include Location header with URL to new resource
     * 
     *              200 OK - Would also work, but 201 is more semantically correct
     *              for creation
     */
    @PostMapping
    public ResponseEntity<UserResponse> registerUser(
            @Valid @RequestBody UserRegistrationRequest request) {

        log.info("POST /api/v1/users - Registering new user: {}", request.getEmail());

        // Convert DTO → Entity
        User user = userMapper.toEntity(request);

        // Call service (handles password hashing, business rules)
        User savedUser = userService.registerUser(user);

        // Convert Entity → Response DTO
        UserResponse response = userMapper.toResponse(savedUser);

        log.info("User registered successfully with ID: {}", savedUser.getId());

        // Return 201 Created with the created user
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // =====================================================
    // READ - GET /api/v1/users/{id}
    // =====================================================

    /**
     * Get a single user by ID.
     * 
     * HTTP: GET /api/v1/users/123
     * Response: UserResponse (JSON) with status 200 OK
     * OR 404 Not Found if user doesn't exist
     * 
     * =====================================================
     * 
     * @PathVariable
     *               =====================================================
     * 
     *               Extracts values from the URL path.
     * 
     *               URL: /api/v1/users/42
     *               Pattern: /api/v1/users/{id}
     *               Result: id = 42
     * 
     *               The {id} in @GetMapping("/{id}") is a path variable.
     * @PathVariable Long id captures the value and converts to Long.
     * 
     *               Type conversion is automatic:
     *               - /users/42 → Long id = 42
     *               - /users/abc → 400 Bad Request (can't convert to Long)
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        log.info("GET /api/v1/users/{} - Fetching user", id);

        return userService.findById(id)
                .map(user -> {
                    UserResponse response = userMapper.toResponse(user);
                    log.debug("Found user: {}", user.getEmail());
                    return ResponseEntity.ok(response); // 200 OK
                })
                .orElseGet(() -> {
                    log.warn("User not found with ID: {}", id);
                    return ResponseEntity.notFound().build(); // 404 Not Found
                });
    }

    /**
     * Get a user by email address.
     * 
     * HTTP: GET /api/v1/users/email/john@example.com
     * 
     * Note: Email in URL path. Alternative: use query parameter
     * GET /api/v1/users?email=john@example.com
     */
    @GetMapping("/email/{email}")
    public ResponseEntity<UserResponse> getUserByEmail(@PathVariable String email) {
        log.info("GET /api/v1/users/email/{} - Fetching user by email", email);

        return userService.findByEmail(email)
                .map(user -> ResponseEntity.ok(userMapper.toResponse(user)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // =====================================================
    // READ ALL - GET /api/v1/users
    // =====================================================

    /**
     * Get all users.
     * 
     * HTTP: GET /api/v1/users
     * Response: List<UserResponse> (JSON array)
     * 
     * ⚠️ PRODUCTION NOTE: This should be paginated!
     * We'll add pagination in a later lesson using Pageable:
     * GET /api/v1/users?page=0&size=10&sort=lastName
     */
    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        log.info("GET /api/v1/users - Fetching all users");

        List<User> users = userService.findAllUsers();
        List<UserResponse> response = userMapper.toResponseList(users);

        log.debug("Returning {} users", response.size());

        return ResponseEntity.ok(response);
    }

    /**
     * Get users by role.
     * 
     * HTTP: GET /api/v1/users/role/DOCTOR
     * 
     * Use case: List all doctors for appointment scheduling.
     */
    @GetMapping("/role/{role}")
    public ResponseEntity<List<UserResponse>> getUsersByRole(@PathVariable String role) {
        log.info("GET /api/v1/users/role/{} - Fetching users by role", role);

        try {
            User.Role userRole = User.Role.valueOf(role.toUpperCase());
            List<User> users = userService.findUsersByRole(userRole);
            return ResponseEntity.ok(userMapper.toResponseList(users));
        } catch (IllegalArgumentException e) {
            log.warn("Invalid role requested: {}", role);
            return ResponseEntity.badRequest().build(); // 400 Bad Request
        }
    }

    /**
     * Search users by name.
     * 
     * HTTP: GET /api/v1/users/search?name=John
     * 
     * =====================================================
     * 
     * @RequestParam
     *               =====================================================
     * 
     *               Extracts values from query string parameters.
     * 
     *               URL: /api/v1/users/search?name=John&active=true
     * 
     * @RequestParam String name → "John"
     * @RequestParam boolean active → true
     * @RequestParam(required=false) opt → null if not provided
     *                               @RequestParam(defaultValue="10") → uses default
     *                               if not provided
     */
    @GetMapping("/search")
    public ResponseEntity<List<UserResponse>> searchUsers(
            @RequestParam String name) {

        log.info("GET /api/v1/users/search?name={} - Searching users", name);

        List<User> users = userService.searchUsersByName(name);
        return ResponseEntity.ok(userMapper.toResponseList(users));
    }

    /**
     * Check if an email is available (for registration form validation).
     * 
     * HTTP: GET /api/v1/users/check-email?email=john@example.com
     * Response: { "available": true } or { "available": false }
     */
    @GetMapping("/check-email")
    public ResponseEntity<EmailAvailability> checkEmailAvailability(
            @RequestParam String email) {

        boolean taken = userService.isEmailTaken(email);
        return ResponseEntity.ok(new EmailAvailability(!taken));
    }

    /**
     * Simple DTO for email availability response.
     * 
     * Using a record (Java 16+) for a simple data carrier.
     * Records automatically generate constructor, getters, equals, hashCode,
     * toString.
     */
    public record EmailAvailability(boolean available) {
    }

    // =====================================================
    // UPDATE - PUT /api/v1/users/{id}
    // =====================================================

    /**
     * Update a user's profile.
     * 
     * HTTP: PUT /api/v1/users/123
     * Request Body: UserUpdateRequest (JSON)
     * Response: Updated UserResponse
     * 
     * =====================================================
     * PUT vs PATCH
     * =====================================================
     * 
     * PUT: Replace the entire resource
     * - Client sends ALL fields, even unchanged ones
     * - Missing fields are set to null/default
     * 
     * PATCH: Partial update
     * - Client sends ONLY the fields to change
     * - Missing fields are left unchanged
     * 
     * We're using PUT here for simplicity.
     * In production, PATCH is often more practical.
     */
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateRequest request) {

        log.info("PUT /api/v1/users/{} - Updating user", id);

        // Build a User object with the updated fields
        User updatedData = User.builder()
                .email(request.getEmail())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .build();

        try {
            User updatedUser = userService.updateUserProfile(id, updatedData);
            return ResponseEntity.ok(userMapper.toResponse(updatedUser));
        } catch (IllegalArgumentException e) {
            log.warn("Update failed: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    // =====================================================
    // DELETE - DELETE /api/v1/users/{id}
    // =====================================================

    /**
     * Disable a user account (soft delete).
     * 
     * HTTP: DELETE /api/v1/users/123
     * Response: 204 No Content (success, no body)
     * 
     * =====================================================
     * 204 No Content
     * =====================================================
     * 
     * Standard response for successful DELETE:
     * - 204 means "success, but nothing to return"
     * - No response body (empty)
     * 
     * Alternative: Return 200 OK with the deleted resource
     * (useful if client needs to confirm what was deleted)
     * 
     * We use DISABLE (soft delete) instead of hard delete
     * for HIPAA compliance in healthcare applications.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> disableUser(@PathVariable Long id) {
        log.info("DELETE /api/v1/users/{} - Disabling user", id);

        try {
            userService.disableUser(id);
            return ResponseEntity.noContent().build(); // 204 No Content
        } catch (IllegalArgumentException e) {
            log.warn("Disable failed: {}", e.getMessage());
            return ResponseEntity.notFound().build(); // 404 Not Found
        }
    }

    // =====================================================
    // ADMIN OPERATIONS
    // =====================================================

    /**
     * Enable a previously disabled user account.
     * 
     * HTTP: PATCH /api/v1/users/123/enable
     * 
     * ⚠️ This should be protected by admin role (we'll add security later)
     */
    @PatchMapping("/{id}/enable")
    public ResponseEntity<Void> enableUser(@PathVariable Long id) {
        log.info("PATCH /api/v1/users/{}/enable - Enabling user", id);

        try {
            userService.enableUser(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Lock a user account (e.g., after too many failed login attempts).
     * 
     * HTTP: PATCH /api/v1/users/123/lock
     */
    @PatchMapping("/{id}/lock")
    public ResponseEntity<Void> lockUser(@PathVariable Long id) {
        log.info("PATCH /api/v1/users/{}/lock - Locking user", id);

        try {
            userService.lockUser(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Unlock a user account.
     * 
     * HTTP: PATCH /api/v1/users/123/unlock
     */
    @PatchMapping("/{id}/unlock")
    public ResponseEntity<Void> unlockUser(@PathVariable Long id) {
        log.info("PATCH /api/v1/users/{}/unlock - Unlocking user", id);

        try {
            userService.unlockUser(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
