package com.ganainy.authservice.controller;

import com.ganainy.authservice.model.dto.CreateUserRequest;
import com.ganainy.authservice.model.dto.UserResponse;
import com.ganainy.authservice.model.entity.User;
import com.ganainy.authservice.service.UserService;

// Lombok for logging
import lombok.extern.slf4j.Slf4j;

// Spring Web annotations
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// Validation
import jakarta.validation.Valid;

import java.util.List;
import java.util.stream.Collectors;

/**
 * UserController - REST API endpoints for User management.
 * 
 * =====================================================
 * WHAT WE'RE BUILDING
 * =====================================================
 * 
 * This controller creates REST API endpoints:
 * POST /api/users → Create a new user
 * GET /api/users/{id} → Get user by ID
 * GET /api/users → List all users
 * PUT /api/users/{id} → Update a user
 * DELETE /api/users/{id} → Delete a user
 * 
 * =====================================================
 * UNDERSTANDING @RestController
 * =====================================================
 * 
 * @RestController is a combination of two annotations:
 *                 1. @Controller - Marks this as a Spring MVC controller
 *                 2. @ResponseBody - All method returns are automatically
 *                 converted to JSON
 * 
 *                 Without @RestController, you'd need @ResponseBody on every
 *                 method:
 * @GetMapping
 * @ResponseBody
 *               public User getUser() { ... }
 * 
 *               With @RestController, JSON conversion is automatic!
 * 
 *               =====================================================
 *               UNDERSTANDING @RequestMapping
 *               =====================================================
 * 
 *               @RequestMapping("/api/users") sets the BASE PATH for all
 *               endpoints in this controller.
 * 
 *               If a method has @GetMapping("/{id}"), the full URL becomes:
 *               /api/users/{id}
 * 
 *               =====================================================
 *               REST PRINCIPLES WE'RE FOLLOWING
 *               =====================================================
 * 
 *               1. Resource-based URLs:
 *               - /api/users (collection)
 *               - /api/users/123 (single resource)
 * 
 *               2. HTTP methods for actions:
 *               - GET = Read
 *               - POST = Create
 *               - PUT = Update (full replacement)
 *               - PATCH = Partial update
 *               - DELETE = Delete
 * 
 *               3. Proper HTTP status codes:
 *               - 200 OK (success)
 *               - 201 Created (resource created)
 *               - 204 No Content (success, no body)
 *               - 400 Bad Request (validation error)
 *               - 404 Not Found (resource doesn't exist)
 *               - 500 Internal Server Error (unexpected error)
 */
@RestController // This class handles HTTP requests and returns JSON
@RequestMapping("/api/users") // Base URL for all endpoints
@Slf4j // Lombok: generates a logger
public class UserController {

    /**
     * Dependency injection via constructor.
     * Spring injects the UserService implementation (UserServiceImpl).
     */
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * =====================================================
     * CREATE USER - POST /api/users
     * =====================================================
     * 
     * @PostMapping marks this method as handling HTTP POST requests.
     * 
     * @RequestBody tells Spring:
     *              "Take the JSON body of the request and convert it to a User
     *              object"
     * 
     * @Valid triggers validation:
     *        - Checks @NotBlank, @Email, @Size annotations on User fields
     *        - If validation fails, Spring returns 400 Bad Request automatically
     * 
     *        ResponseEntity<User> allows us to:
     *        - Set the HTTP status code
     *        - Set headers
     *        - Return the body
     * 
     *        Example request:
     *        POST /api/users
     *        Content-Type: application/json
     * 
     *        {
     *        "email": "john@example.com",
     *        "password": "SecurePassword123",
     *        "firstName": "John",
     *        "lastName": "Doe"
     *        }
     * 
     *        Example response (201 Created):
     *        {
     *        "id": 1,
     *        "email": "john@example.com",
     *        "firstName": "John",
     *        "lastName": "Doe",
     *        "role": "PATIENT",
     *        ...
     *        }
     */
    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        log.info("REST request to create user: {}", request.email());

        // Convert DTO to Entity
        // Note: We only set the fields the client is allowed to provide
        // Role, enabled, etc. are set to defaults by the entity
        User user = new User();
        user.setEmail(request.email());
        user.setPassword(request.password());
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());

        User createdUser = userService.createUser(user);

        // Convert Entity to Response DTO (hides password hash)
        UserResponse response = UserResponse.fromEntity(createdUser);

        // Return 201 Created with the created user in the body
        return ResponseEntity
                .status(HttpStatus.CREATED) // 201
                .body(response);
    }

    /**
     * =====================================================
     * GET USER BY ID - GET /api/users/{id}
     * =====================================================
     * 
     * @GetMapping("/{id}") maps GET requests with a path variable.
     * 
     * @PathVariable extracts the {id} from the URL:
     *               GET /api/users/123 → id = 123
     * 
     *               Example request:
     *               GET /api/users/1
     * 
     *               Example response (200 OK):
     *               {
     *               "id": 1,
     *               "email": "john@example.com",
     *               ...
     *               }
     * 
     *               Or (404 Not Found) if user doesn't exist.
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        log.info("REST request to get user by ID: {}", id);

        return userService.findUserById(id)
                // If found, convert to DTO and return 200 OK
                .map(user -> ResponseEntity.ok(UserResponse.fromEntity(user)))
                // If not found, return 404 Not Found
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * =====================================================
     * GET ALL USERS - GET /api/users
     * =====================================================
     * 
     * Simple endpoint that returns all users.
     * 
     * ⚠️ WARNING: In production, this should use pagination!
     * We'll add pagination in upcoming tasks.
     * 
     * Example request:
     * GET /api/users
     * 
     * Example response (200 OK):
     * [
     * { "id": 1, "email": "john@example.com", ... },
     * { "id": 2, "email": "jane@example.com", ... }
     * ]
     */
    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        log.info("REST request to get all users");

        List<UserResponse> users = userService.findAllUsers().stream()
                .map(UserResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }

    /**
     * =====================================================
     * UPDATE USER - PUT /api/users/{id}
     * =====================================================
     * 
     * @PutMapping is for full resource updates.
     *             The client sends the complete updated resource.
     * 
     *             Example request:
     *             PUT /api/users/1
     *             Content-Type: application/json
     * 
     *             {
     *             "email": "john.updated@example.com",
     *             "firstName": "John",
     *             "lastName": "Doe Updated"
     *             }
     * 
     *             Example response (200 OK):
     *             {
     *             "id": 1,
     *             "email": "john.updated@example.com",
     *             ...
     *             }
     */
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody User user) {

        log.info("REST request to update user ID {}: {}", id, user.getEmail());

        User updatedUser = userService.updateUser(id, user);
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * =====================================================
     * DELETE USER - DELETE /api/users/{id}
     * =====================================================
     * 
     * @DeleteMapping handles HTTP DELETE requests.
     * 
     *                Returns 204 No Content on success (no body needed).
     * 
     *                Example request:
     *                DELETE /api/users/1
     * 
     *                Example response:
     *                204 No Content (empty body)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        log.info("REST request to delete user ID: {}", id);

        userService.deleteUser(id);

        // 204 No Content - success, but no body to return
        return ResponseEntity.noContent().build();
    }

    /**
     * =====================================================
     * CHECK EMAIL EXISTS - GET /api/users/exists?email=...
     * =====================================================
     * 
     * @RequestParam extracts query parameters from the URL:
     *               GET /api/users/exists?email=john@example.com → email =
     *               "john@example.com"
     * 
     *               Useful for client-side validation during registration.
     * 
     *               Example request:
     *               GET /api/users/exists?email=john@example.com
     * 
     *               Example response (200 OK):
     *               { "exists": true }
     */
    @GetMapping("/exists")
    public ResponseEntity<EmailExistsResponse> checkEmailExists(@RequestParam String email) {
        log.info("REST request to check if email exists: {}", email);

        boolean exists = userService.emailExists(email);
        return ResponseEntity.ok(new EmailExistsResponse(exists));
    }

    /**
     * Simple record (Java 14+) for the email exists response.
     * A record automatically generates constructor, getters, equals, hashCode,
     * toString.
     */
    record EmailExistsResponse(boolean exists) {
    }
}
