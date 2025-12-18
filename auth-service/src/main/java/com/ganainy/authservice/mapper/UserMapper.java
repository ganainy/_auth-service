package com.ganainy.authservice.mapper;

import com.ganainy.authservice.model.dto.UserRegistrationRequest;
import com.ganainy.authservice.model.dto.UserResponse;
import com.ganainy.authservice.model.entity.User;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * UserMapper - Converts between User entity and DTOs.
 * 
 * =====================================================
 * WHAT IS A MAPPER?
 * =====================================================
 * 
 * A Mapper is responsible for converting between different object types:
 * - Entity → DTO (for API responses)
 * - DTO → Entity (for saving to database)
 * 
 * Why not do this in the Service or Controller?
 * - Single Responsibility Principle: Mapper only maps
 * - Reusability: Same mapping logic used everywhere
 * - Testability: Easy to test mapping in isolation
 * - Clean code: Service/Controller stay focused on their jobs
 * 
 * =====================================================
 * MANUAL vs LIBRARY MAPPING
 * =====================================================
 * 
 * This is a MANUAL mapper - we write the mapping code ourselves.
 * 
 * Alternatives:
 * - MapStruct: Generates mapper code at compile time (fastest!)
 * - ModelMapper: Uses reflection at runtime (slower but flexible)
 * - Dozer: Another reflection-based mapper
 * 
 * We'll use manual mapping for now because:
 * 1. It's explicit - you see exactly what's happening
 * 2. No extra dependencies
 * 3. Good for learning
 * 
 * In production with many DTOs, MapStruct is recommended
 * because it generates efficient code and catches errors at compile time.
 * 
 * =====================================================
 * 
 * @Component
 *            =====================================================
 * 
 * @Component is the generic Spring bean annotation.
 *            It marks this class as a Spring-managed bean.
 * 
 *            @Service, @Repository, @Controller are all specialized @Component:
 * @Component → generic bean
 * @Service → business logic bean
 * @Repository → data access bean
 * @Controller → HTTP handler bean
 * 
 *             For mappers, @Component is appropriate since they don't fit
 *             the other categories.
 */
@Component
public class UserMapper {

    /**
     * Convert registration request DTO to User entity.
     * 
     * Used when: Client sends registration data → we need an entity to save
     * 
     * Note: Password is set as plain text here.
     * The UserService will hash it before saving!
     * 
     * @param request The registration request from the client
     * @return User entity (not yet saved, no ID)
     */
    public User toEntity(UserRegistrationRequest request) {
        if (request == null) {
            return null;
        }

        User.UserBuilder builder = User.builder()
                .email(request.getEmail())
                .password(request.getPassword()) // Plain text - service will hash it
                .firstName(request.getFirstName())
                .lastName(request.getLastName());

        // Handle optional role
        if (request.getRole() != null && !request.getRole().isBlank()) {
            try {
                builder.role(User.Role.valueOf(request.getRole().toUpperCase()));
            } catch (IllegalArgumentException e) {
                // Invalid role string - service will set default PATIENT
                // We could also throw a validation exception here
            }
        }

        return builder.build();
    }

    /**
     * Convert User entity to response DTO.
     * 
     * Used when: We fetched a user from DB → need to return to client
     * 
     * CRITICAL: Notice we NEVER include the password!
     * 
     * @param user The user entity from the database
     * @return Safe response DTO (no password!)
     */
    public UserResponse toResponse(User user) {
        if (user == null) {
            return null;
        }

        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                // NO PASSWORD! Never ever include password in response!
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .fullName(user.getFirstName() + " " + user.getLastName()) // Computed field!
                .role(user.getRole() != null ? user.getRole().name() : null)
                .enabled(user.isEnabled())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    /**
     * Convert a list of User entities to response DTOs.
     * 
     * Uses Java Streams to map each entity to a DTO.
     * 
     * @param users List of user entities
     * @return List of response DTOs
     */
    public List<UserResponse> toResponseList(List<User> users) {
        if (users == null) {
            return List.of(); // Return empty list, not null
        }

        return users.stream()
                .map(this::toResponse) // Method reference: calls toResponse for each user
                .collect(Collectors.toList());
    }
}
