package com.example.step_project_beck_spring.config;

import com.example.step_project_beck_spring.filter.JwtAuthenticationFilter;
import com.example.step_project_beck_spring.handler.OAuth2AuthenticationSuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Вимикаємо CSRF (бо stateless + JWT)
                .csrf(csrf -> csrf.disable())

                // CORS налаштування (дозволяємо Vercel + credentials)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // Авторизація запитів
                .authorizeHttpRequests(auth -> auth
                        // Swagger UI та API документація доступні без автентифікації
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**", "/swagger-resources/**", "/webjars/**").permitAll()
                        
                        // Публічні сторінки та статичні файли
                        .requestMatchers("/", "/index.html", "/login.html", "/chat.html",
                                "/favicon.ico", "/**/*.html", "/**/*.css", "/**/*.js").permitAll()

                        // Публічні API (реєстрація, логін, Google OAuth)
                        .requestMatchers("/api/auth/**", "/oauth2/**", "/login/oauth2/code/**").permitAll()

                        // WebSocket
                        .requestMatchers("/ws/**").permitAll()

                        // Захищені API — тільки авторизовані
                        .requestMatchers("/api/upload/**",
                                "/api/user/**",
                                "/api/chat/**",
                                "/api/posts/**",
                                "/api/comments/**",
                                "/api/likes/**",
                                "/api/follow/**",
                                "/api/notifications/**").authenticated()

                        .requestMatchers("/api/auth/logout").permitAll()

                        // Все інше — вимагає авторизації
                        .anyRequest().authenticated()
                )

                // Без сесій (stateless)
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // OAuth2 Login (Google)
                .oauth2Login(oauth2 -> oauth2
                        .successHandler(oAuth2AuthenticationSuccessHandler)
                        // Дозволяємо доступ до OAuth без авторизації
                        .permitAll()
                )

                // Обробка помилок авторизації
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            // Якщо запит на API (/api/...), повертаємо 401 JSON
                            if (request.getRequestURI().startsWith("/api/")) {
                                response.setStatus(401);
                                response.setContentType("application/json");
                                response.getWriter().write("{\"error\": \"Unauthorized\", \"message\": \"Please login via /oauth2/authorization/google\"}");
                            } else {
                                // Для звичайних запитів — редирект на Google
                                response.sendRedirect("/oauth2/authorization/google");
                            }
                        })
                )

                // Додаємо JWT-фільтр перед стандартним
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOrigins(List.of(
                "https://widi-rho.vercel.app",      // продакшн
                "http://localhost:5173"             // локальний фронт
        ));
        // Дозволяємо методи, які потрібні
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        // Дозволяємо всі заголовки
        config.setAllowedHeaders(List.of("*"));
        // Важливо: дозволяємо передачу cookie (credentials)
        config.setAllowCredentials(true);
        // Дозволяємо бачити Set-Cookie у відповіді
        config.setExposedHeaders(List.of("Set-Cookie"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}