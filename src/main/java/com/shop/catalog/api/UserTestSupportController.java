package com.shop.catalog.api;

import com.shop.catalog.domain.User;
import com.shop.catalog.domain.UserRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Test-support controller for managing test users with custom margins.
 * Only active under the "test" profile, so it is never exposed in production.
 */
@RestController
@RequestMapping("/test-support/users")
@Profile("test")
public class UserTestSupportController {

    private final UserRepository userRepository;

    public UserTestSupportController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public record CreateUserRequest(String username, BigDecimal marginPercent) {}

    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody CreateUserRequest request) {
        if (request.username() == null || request.username().isBlank()) {
            throw new IllegalArgumentException("username must not be blank");
        }
        if (request.marginPercent() == null || request.marginPercent().signum() < 0) {
            throw new IllegalArgumentException("marginPercent must be a non-negative number");
        }
        User user = new User(UUID.randomUUID(), request.username().trim(), request.marginPercent());
        User saved = userRepository.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping
    public ResponseEntity<List<User>> allUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(@PathVariable UUID id) {
        return userRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAll() {
        userRepository.deleteAll();
    }
}
