package com.example.step_project_beck_spring.service;

import com.example.step_project_beck_spring.request.AuthResponse;
import com.example.step_project_beck_spring.request.LoginRequest;
import com.example.step_project_beck_spring.request.RegisterRequest;
import com.example.step_project_beck_spring.dto.VerifyEmailRequest;
import com.example.step_project_beck_spring.entities.User;
import com.example.step_project_beck_spring.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailService emailService;
    private final AuthenticationManager authenticationManager;

    /** РЕЄСТРАЦІЯ */
    @Transactional // Якщо пошта не відправиться, юзер не збережеться в БД
    public void register(RegisterRequest request) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new RuntimeException("Email already taken / Цей email вже зайнятий");
        }

        String code = String.valueOf(100000 + new Random().nextInt(900000));
        log.info("TEMPORARY CODE for {}: {}", request.email(), code);

        User user = User.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .birthDate(request.birthDate())
                .emailVerified(false)
                .verificationCode(code)
                .build();

        userRepository.save(user);

        // Прибрав try-catch.
        // Тепер якщо тут виникне помилка (немає SMTP) транзакція відкотить save(user).
        emailService.sendVerificationEmail(request.email(), code);
    }

    /** ПІДТВЕРДЖЕННЯ ПОШТИ */
    public AuthResponse verifyEmail(VerifyEmailRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getVerificationCode() == null || !user.getVerificationCode().equals(request.verificationCode())) {
            throw new RuntimeException("Invalid verification code / Невірний код");
        }

        user.setEmailVerified(true);
        user.setVerificationCode(null);
        userRepository.save(user);

        String token = jwtService.generateToken(user, false);
        return new AuthResponse(token);
    }

    /** ЛОГІН */
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Обов'язкова перевірка
        if (!user.isEmailVerified()) {
            throw new RuntimeException("Email not verified! Please check your email.");
        }

        String token = jwtService.generateToken(user, request.isRememberMe());
        return new AuthResponse(token);
    }
}