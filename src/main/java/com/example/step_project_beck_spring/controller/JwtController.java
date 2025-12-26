package com.example.step_project_beck_spring.controller;

import com.example.step_project_beck_spring.entities.User;
import com.example.step_project_beck_spring.repository.UserRepository;
import com.example.step_project_beck_spring.request.AuthResponse;
import com.example.step_project_beck_spring.request.LoginRequest;
import com.example.step_project_beck_spring.request.RegisterRequest;
import com.example.step_project_beck_spring.service.JwtService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class JwtController {

    // AuthenticationManager — для перевірки логіну/паролю
    private final AuthenticationManager authenticationManager;

    // JwtService — наш сервіс для генерації та перевірки JWT токенів
    private final JwtService jwtService;
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public JwtController(AuthenticationManager authenticationManager, 
                        JwtService jwtService,
                        UserRepository userRepository,
                        PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Реєстрація нового користувача.
     * Створює користувача з emailVerified = true для тестування.
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody @Valid RegisterRequest request) {
        try {
            // Перевіряємо, чи користувач з таким email вже існує
            if (userRepository.findByEmail(request.getEmail()).isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "User with this email already exists");
                return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
            }

            // Створюємо нового користувача
            // НЕ встановлюємо ID вручну - Hibernate згенерує його автоматично через @GeneratedValue
            User user = new User();
            user.setEmail(request.getEmail());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setFirstName(request.getFirstName());
            user.setLastName(request.getLastName());
            user.setEmailVerified(true); // Для тестування автоматично підтверджуємо email

            user = userRepository.save(user);

            // Генеруємо токен для нового користувача
            String token = jwtService.generateToken(user, false);

            return ResponseEntity.ok(new AuthResponse(token));
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Registration failed: " + e.getMessage());
            e.printStackTrace(); // Для дебагу
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Обробка помилок валідації
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            errors.put(error.getField(), error.getDefaultMessage());
        });
        Map<String, String> response = new HashMap<>();
        response.put("error", errors.values().iterator().next()); // Перша помилка
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Приймає LoginRequest (email, пароль, rememberMe).
     * Спочатку перевіряєо логін/пароль через AuthenticationManager.
     *  Якщо успішно то отримує User із контексту.
     *  Генерує JWT токен (на 6 годин або 7 днів, залежно від rememberMe).
     *   Повертає AuthResponse із токеном клієнту.
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid LoginRequest request) {
        try {
            System.out.println("=== LOGIN ATTEMPT ===");
            System.out.println("Email: " + request.getEmail());
            System.out.println("Password length: " + (request.getPassword() != null ? request.getPassword().length() : "null"));
            
            // Спочатку перевіряємо, чи користувач існує та чи підтверджений email
            User existingUser = userRepository.findByEmail(request.getEmail()).orElse(null);
            if (existingUser == null) {
                System.out.println("❌ User not found with email: " + request.getEmail());
                Map<String, String> error = new HashMap<>();
                error.put("error", "Invalid email or password");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            System.out.println("✅ User found: " + existingUser.getEmail());
            System.out.println("Email verified: " + existingUser.isEmailVerified());
            
            if (!existingUser.isEmailVerified()) {
                System.out.println("❌ Email not verified for user: " + existingUser.getEmail());
                Map<String, String> error = new HashMap<>();
                error.put("error", "Email not verified. Please verify your email first.");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            // Перевіряємо пароль вручну для дебагу
            boolean passwordMatches = passwordEncoder.matches(request.getPassword(), existingUser.getPassword());
            System.out.println("Password matches: " + passwordMatches);
            if (!passwordMatches) {
                System.out.println("❌ Password does not match for user: " + existingUser.getEmail());
                System.out.println("Stored password hash: " + existingUser.getPassword());
                Map<String, String> error = new HashMap<>();
                error.put("error", "Invalid email or password");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Перевірка логіну/паролю через AuthenticationManager
            System.out.println("Attempting authentication via AuthenticationManager...");
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
            System.out.println("✅ Authentication successful");

            // Якщо успішно — отримуємо користувача
            User user = (User) auth.getPrincipal();
            System.out.println("User from auth: " + user.getEmail());

            // Генеруємо токен із потрібним часом життя
            String token = jwtService.generateToken(user, request.isRememberMe());
            System.out.println("✅ Token generated successfully");

            // Відправляємо клієнту токен у відповіді
            return ResponseEntity.ok(new AuthResponse(token));
        } catch (BadCredentialsException e) {
            System.err.println("❌ BadCredentialsException: " + e.getMessage());
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", "Invalid email or password");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        } catch (DisabledException e) {
            System.err.println("❌ DisabledException: " + e.getMessage());
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", "Email not verified. Please verify your email first.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        } catch (Exception e) {
            System.err.println("❌ Unexpected error during login: " + e.getMessage());
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", "Login failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}




