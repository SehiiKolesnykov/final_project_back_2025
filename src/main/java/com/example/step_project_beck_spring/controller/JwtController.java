package com.example.step_project_beck_spring.controller;

import com.example.step_project_beck_spring.request.LoginRequest;
import com.example.step_project_beck_spring.request.RegisterRequest;
import com.example.step_project_beck_spring.service.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.time.Duration;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "API для автентифікації та реєстрації користувачів")
public class JwtController {

    private final AuthenticationService authenticationService;

    private static final int SHORT_EXPIRATION_SECONDS = 6 * 3600;   // 6 годин
    private static final int LONG_EXPIRATION_SECONDS = 7 * 24 * 3600; // 7 днів

    @Operation(summary = "Реєстрація нового користувача", description = "Створює нового користувача та повертає JWT токен")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Користувач успішно зареєстрований"),
            @ApiResponse(responseCode = "409", description = "Користувач з таким email вже існує"),
            @ApiResponse(responseCode = "400", description = "Невірні дані реєстрації")
    })
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody @Valid RegisterRequest request) {
        // Логіка спрощена, помилки ловить GlobalExceptionHandler
        var authResponse = authenticationService.register(request);

        ResponseCookie jwtCookie = createJwtCookie(authResponse.token(), false);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                .body(authResponse);
    }

    @Operation(summary = "Автентифікація користувача", description = "Перевіряє email та пароль, повертає JWT токен")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успішна автентифікація, повертається JWT токен"),
            @ApiResponse(responseCode = "401", description = "Невірний email або пароль")
    })
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid LoginRequest request) {
        var authResponse = authenticationService.login(request);

        ResponseCookie jwtCookie = createJwtCookie(authResponse.token(), request.isRememberMe());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                .body(authResponse);
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
}