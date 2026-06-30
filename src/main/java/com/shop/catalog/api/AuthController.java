package com.shop.catalog.api;

import java.math.BigDecimal;
import java.time.Duration;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.shop.catalog.service.AuthService;
import com.shop.catalog.service.AuthService.SessionData;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

@RestController
@RequestMapping("/api")
public class AuthController {

    static final String SESSION_COOKIE = "SHOP_SESSION";
    private static final Duration SESSION_MAX_AGE = Duration.ofHours(2);

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    public record LoginRequest(@NotBlank String username) {}

    public record LoginResponse(String username, String displayName, BigDecimal margin) {}

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        SessionData session = authService.login(request.username());

        ResponseCookie cookie = ResponseCookie.from(SESSION_COOKIE, session.sessionToken())
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(SESSION_MAX_AGE)
                .sameSite("Lax")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(new LoginResponse(
                        session.username(),
                        session.displayName(),
                        session.margin()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@org.springframework.web.bind.annotation.CookieValue(
            name = SESSION_COOKIE, required = false) String sessionToken) {
        if (sessionToken != null) {
            authService.logout(sessionToken);
        }
        ResponseCookie cookie = ResponseCookie.from(SESSION_COOKIE, "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(Duration.ZERO)
                .sameSite("Lax")
                .build();
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .build();
    }
}
