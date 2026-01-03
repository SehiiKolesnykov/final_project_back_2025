package com.example.step_project_beck_spring.service;

import com.example.step_project_beck_spring.dto.*;
import com.example.step_project_beck_spring.entities.User;
import com.example.step_project_beck_spring.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; //  імпорт для логування
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j // анотація додає можливість писати log.info()
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailService emailService;
    private final AuthenticationManager authenticationManager;

    /** РЕЄСТРАЦІЯ (Використовує Record - request.email()) */
    public void register(RegisterRequest request) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new RuntimeException("Email already taken / Цей email вже зайнятий");
        }

        // Генерація 6-значного коду
        String code = String.valueOf(100000 + new Random().nextInt(900000));

        // ДОДАВ ЛОГ код з'явиться в консолі при реєстрації
        log.info(" TEMPORARY CODE for {}: {}", request.email(), code);

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
        emailService.sendVerificationEmail(request.email(), code);
    }

    /** ПІДТВЕРДЖЕННЯ ПОШТИ (Record - request.email()) */
    public AuthResponse verifyEmail(VerifyEmailRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getVerificationCode() == null || !user.getVerificationCode().equals(request.verificationCode())) {
            throw new RuntimeException("Invalid verification code / Невірний код");
        }

        user.setEmailVerified(true);
        user.setVerificationCode(null);
        userRepository.save(user);

        // (false бо це не "запам'ятати мене" а просто автологін)
        String token = jwtService.generateToken(user, false);
        return new AuthResponse(token);
    }

    /** ЛОГІН (Використовує Class/Lombok - request.getEmail() та isRememberMe()) */
    public AuthResponse login(LoginRequest request) {
        // Spring Security перевіряє пароль
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        //  реквст юзера
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Перевірка, чи підтвердив пошту
        if (!user.isEmailVerified()) {
            throw new RuntimeException("Email not verified! Please check your email.");
        }
        // Генеруємо токен
        String token = jwtService.generateToken(user, request.isRememberMe());
        return new AuthResponse(token);
    }
}