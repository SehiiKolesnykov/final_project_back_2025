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
                        // Swagger UI та API документація доступні без автентифікації
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**", "/swagger-resources/**", "/webjars/**").permitAll()
                        // Статичні файли (HTML, CSS, JS) доступні без автентифікації
                        .requestMatchers("/", "/index.html", "/login.html", "/chat.html", "/*.html", "/*.css", "/*.js").permitAll()//це для власного фронта
                        // Доступ за токеном — автентифікація та реєстрація (ВАЖЛИВО: має бути перед іншими правилами)
                        .requestMatchers("/api/auth/**").permitAll()
                        // WebSocket endpoint для чату
                        .requestMatchers("/ws/**").permitAll()
                        // Підписування завантажень на Cloudinary — тільки авторизовані користувачі
                        .requestMatchers("/api/upload/**").authenticated()
                        // Всі дії з профілем користувача — тільки авторизовані
                        .requestMatchers("/api/user/**").authenticated()
//                         API чату — тільки авторизовані користувачі
                        .requestMatchers("/api/chat/**").authenticated()
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
        // УВАГА: allowCredentials(true) не працює з allowedOriginPatterns("*") в деяких версіях
        // Якщо є проблеми з CORS, встановіть конкретний origin замість "*"
        config.setAllowCredentials(false); // Змінимо на false, щоб уникнути конфліктів

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // Застосовуємо ці налаштування до всіх шляхів
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    /**
     * AuthenticationManager потрібен для обробки логіну (JwtController).
     * Беремо готовий з AuthenticationConfiguration.
     * Spring автоматично використовує UserDetailsService для завантаження користувачів.
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