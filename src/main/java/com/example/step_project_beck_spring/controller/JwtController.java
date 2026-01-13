package com.example.step_project_beck_spring.controller;

import com.example.step_project_beck_spring.request.AuthResponse;
import com.example.step_project_beck_spring.request.LoginRequest;
import com.example.step_project_beck_spring.request.RegisterRequest;
import com.example.step_project_beck_spring.service.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class JwtController {

    // Контролер має звертатися до Сервісу, а не робити все сам
    private final AuthenticationService authenticationService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody @Valid RegisterRequest request) {
        try {
            // Викликаємо сервіс (збереження юзера + відправка листа)
            authenticationService.register(request);

            // Повертаємо повідомлення а не токен
            return ResponseEntity.ok(Map.of("message", "Registration successful. Please check your email for verification code."));

        } catch (RuntimeException e) {
            // Обробка помилок (наприклад, email зайнятий)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Registration failed: " + e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid LoginRequest request) {
        try {
            // Делегуємо логін сервісу (там є перевірка isEmailVerified)
            AuthResponse response = authenticationService.login(request);
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            // Це зловить помилку "Email not verified" або "Bad credentials"
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Login failed: " + e.getMessage()));
        }
    }

    // цей метод щоб Postman міг відправити код
    @PostMapping("/verify")
    public ResponseEntity<?> verify(@RequestBody com.example.step_project_beck_spring.dto.VerifyEmailRequest request) {
        return ResponseEntity.ok(authenticationService.verifyEmail(request));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }
}