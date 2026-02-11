// src/main/java/com/example/step_project_beck_spring/controller/JwtController.java
package com.example.step_project_beck_spring.controller;

import com.example.step_project_beck_spring.request.AuthResponse;
import com.example.step_project_beck_spring.request.LoginRequest;
import com.example.step_project_beck_spring.request.RegisterRequest;
import com.example.step_project_beck_spring.service.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class JwtController {

    private final AuthenticationService authenticationService;

    private static final int SHORT_EXPIRATION_SECONDS = 6 * 3600;   // 6 годин
    private static final int LONG_EXPIRATION_SECONDS = 7 * 24 * 3600; // 7 днів

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody @Valid RegisterRequest request) {
        try {
            AuthResponse authResponse = authenticationService.register(request);

            // Для register — короткий термін (6 годин)
            ResponseCookie jwtCookie = createJwtCookie(authResponse.token(), false);

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                    .body(authResponse);

        } catch (RuntimeException e) {
            String msg = e.getMessage().toLowerCase();
            if (msg.contains("taken") || msg.contains("exists")) {
                return ResponseEntity.status(409).body(Map.of("error", e.getMessage()));
            }
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid LoginRequest request) {
        try {
            AuthResponse authResponse = authenticationService.login(request);

            ResponseCookie jwtCookie = createJwtCookie(authResponse.token(), request.isRememberMe());

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                    .body(authResponse);

        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        }
    }

    private ResponseCookie createJwtCookie(String token, boolean rememberMe) {
        int maxAge = rememberMe ? LONG_EXPIRATION_SECONDS : SHORT_EXPIRATION_SECONDS;

        return ResponseCookie.from("jwt", token)
                .httpOnly(true)
                .secure(true)                    // HTTPS на Render
                .sameSite("None")
                .path("/")
                .maxAge(Duration.ofSeconds(maxAge))
                .build();
    }

    // Валідація
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );
        return ResponseEntity.badRequest().body(errors);
    }
}