package com.example.step_project_beck_spring.controller;

import com.example.step_project_beck_spring.entities.User;
import com.example.step_project_beck_spring.request.AuthResponse;
import com.example.step_project_beck_spring.request.LoginRequest;
import com.example.step_project_beck_spring.service.JwtService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class JwtController {

    // AuthenticationManager — для перевірки логіну/паролю
    private final AuthenticationManager authenticationManager;

    // JwtService — наш сервіс для генерації та перевірки JWT токенів
    private final JwtService jwtService;

    public JwtController(AuthenticationManager authenticationManager, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    /**
     * Приймає LoginRequest (email, пароль, rememberMe).
     * Спочатку перевіряєо логін/пароль через AuthenticationManager.
     *  Якщо успішно то отримує User із контексту.
     *  Генерує JWT токен (на 6 годин або 7 днів, залежно від rememberMe).
     *   Повертає AuthResponse із токеном клієнту.
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid LoginRequest request) {
        // Перевірка логіну/паролю
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        // Якщо успішно — отримуємо користувача
        User user = (User) auth.getPrincipal();

        // Генеруємо токен із потрібним часом життя
        String token = jwtService.generateToken(user, request.isRememberMe());

        // Відправляємо клієнту токен у відповіді
        return ResponseEntity.ok(new AuthResponse(token));
    }
}




