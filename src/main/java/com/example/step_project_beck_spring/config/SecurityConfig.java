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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Головна конфігурація Spring Security для нашого REST API.
 * Використовуємо JWT + stateless сесії, тому CSRF вимкнено, сесії не зберігаються.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // Фільтр, який перевіряє JWT-токен у кожному запиті
    private final JwtAuthenticationFilter jwtAuthFilter;

    // Spring сам інжектить наш JwtAuthenticationFilter (він зареєстрований як @Component)
    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    /**
     * Основний ланцюжок фільтрів безпеки.
     * Тут визначаємо всі правила доступу, CORS, сесії тощо.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Підключаємо власну CORS-конфігурацію
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // CSRF не потрібен у stateless REST API з JWT
                .csrf(csrf -> csrf.disable())

                // Правила авторизації для різних ендпоінтів
                .authorizeHttpRequests(auth -> auth
                        // Доступ за токеном — автентифікація та реєстрація
                        .requestMatchers("/api/auth/**").permitAll()
                        // Підписування завантажень на Cloudinary — тільки авторизовані користувачі
                        .requestMatchers("/api/upload/**").authenticated()
                        // Всі дії з профілем користувача — тільки авторизовані
                        .requestMatchers("/api/user/**").authenticated()
                        // Усі інші запити також вимагають автентифікацію
                        .anyRequest().authenticated()
                )

                // Не створюємо та не зберігаємо HTTP-сесію — працюємо тільки з JWT
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Додаємо наш JWT-фільтр ПЕРЕД стандартним фільтром логін/пароль
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Налаштування CORS.
     * Наразі дозволено все (для розробки). У продакшені вкажемо точний origin фронтенду,
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // Дозволяємо будь-який origin (можна замінити на конкретний домен)
        config.setAllowedOriginPatterns(List.of("*"));
        // Дозволені HTTP-методи
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        // Дозволяємо будь-які заголовки
        config.setAllowedHeaders(List.of("*"));
        // Дозволяємо передавати куки/авторизаційні заголовки (важливо для credentials)
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // Застосовуємо ці налаштування до всіх шляхів
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    /**
     * AuthenticationManager потрібен для обробки логіну (JwtAuthenticationController).
     * Беремо готовий з AuthenticationConfiguration.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Кодувальник паролів.
     * BCrypt з силою 12 — гарний баланс між безпекою та швидкістю.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}