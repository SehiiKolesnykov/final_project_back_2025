package com.example.step_project_beck_spring.config;

import com.example.step_project_beck_spring.filter.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }
    /**
     * Основний ланцюг безпеки (SecurityFilterChain).
     * Вимикаємо CSRF,Дозволяємо публічний доступ до /api/auth/** та /api/user/** (реєстрація, логін, перегляд профілю).
     *  Всі інші запити вимагають автентифікації.
     *  - Додаємо наш JwtAuthenticationFilter перед стандартним UsernamePasswordAuthenticationFilter.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // CSRF не потрібен для REST API
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**", "/api/user/**").permitAll() // публічні ендпоінти
                        .anyRequest().authenticated() // решта потребує JWT
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // без сесій
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class); // додаємо наш JWT-фільтр

        return http.build();
    }

    /**
     * AuthenticationManager — використовується для логіну.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * PasswordEncoder — алгоритм для хешування паролів.
     * Використовуємо BCrypt із силою 12 (достатньо безпечно для більшості випадків).
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}



