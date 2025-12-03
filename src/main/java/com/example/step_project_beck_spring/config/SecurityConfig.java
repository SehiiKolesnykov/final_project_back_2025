package com.example.step_project_beck_spring.config;

import org.springframework.context.annotation.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class SecurityConfig {

    /**
     * BCrypt — алгоритм для паролів
     * Число 12 — це work factor, тобто складність шифрування. Чим більше — тим важче зламати, але повільніше працює.
     * 10–12 — я вважаю що цього буде достатньо
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}
