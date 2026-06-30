package com.shop.catalog.service;

import com.shop.catalog.domain.User;
import com.shop.catalog.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthService {

    private static final long SESSION_TTL_MS = 8 * 60 * 60 * 1000L;

    private final UserRepository userRepository;
    private final ConcurrentHashMap<String, Session> sessions = new ConcurrentHashMap<>();

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public LoginResult login(String username) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username must not be blank");
        }
        User user = userRepository.findByName(username.trim())
                .orElseThrow(() -> new IllegalArgumentException("Unknown user: " + username));
        String token = UUID.randomUUID().toString();
        long expiresAt = System.currentTimeMillis() + SESSION_TTL_MS;
        sessions.put(token, new Session(user.getId(), user.getName(), user.getMargin(), expiresAt));
        return new LoginResult(token, user.getName(), user.getMargin());
    }

    public Optional<Session> getSession(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        Session session = sessions.get(token);
        if (session == null) {
            return Optional.empty();
        }
        if (System.currentTimeMillis() > session.expiresAt()) {
            sessions.remove(token);
            return Optional.empty();
        }
        return Optional.of(session);
    }

    public void logout(String token) {
        if (token != null && !token.isBlank()) {
            sessions.remove(token);
        }
    }

    public record LoginResult(String token, String username, BigDecimal margin) {
    }

    public record Session(Long userId, String username, BigDecimal margin, long expiresAt) {
    }
}
