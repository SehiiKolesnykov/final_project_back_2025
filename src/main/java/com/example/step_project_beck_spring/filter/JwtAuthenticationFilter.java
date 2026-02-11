// src/main/java/com/example/step_project_beck_spring/filter/JwtAuthenticationFilter.java
package com.example.step_project_beck_spring.filter;

import com.example.step_project_beck_spring.entities.User;
import com.example.step_project_beck_spring.repository.UserRepository;
import com.example.step_project_beck_spring.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Головний JWT-фільтр.
 * Виконується при кожному запиті (один раз — тому extends OncePerRequestFilter).
 * Підтримує тільки HttpOnly-куку з назвою "jwt" (перевірка only cookie).
 *
 * Якщо токен валідний — автоматично "логінить" користувача в SecurityContext.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;         // для розбору та валідації JWT
    private final UserRepository userRepository; // щоб знайти користувача за email з токена

    // Spring сам інжектить залежності через конструктор
    public JwtAuthenticationFilter(JwtService jwtService, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    /**
     * Визначає, чи потрібно застосовувати фільтр для цього запиту.
     * Повністю вимикаємо фільтр для /api/auth/** та /ws/**
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path != null && (path.startsWith("/api/auth/") || path.startsWith("/ws/"));
    }

    /**
     * Основна логіка фільтра.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String jwt = null;

        // Шукаємо тільки в куках (назва куки саме "jwt")
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("jwt".equals(cookie.getName())) {
                    jwt = cookie.getValue();
                    break; // знайшли — виходимо з циклу
                }
            }
        }

        // Якщо токена взагалі немає — просто продовжуємо ланцюжок (запит піде як анонімний)
        if (jwt == null) {
            filterChain.doFilter(request, response);
            return;
        }

        // Витягуємо email (або інший username) з JWT
        String userEmail = jwtService.extractUsername(jwt);

        // Перевіряємо, чи є email і чи користувач ще не автентифікований у цьому запиті
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // Шукаємо користувача в базі за email з токена
            User user = userRepository.findByEmail(userEmail).orElse(null);

            // Якщо користувач існує і токен валідний (не прострочений, підпис правильний тощо)
            if (user != null && jwtService.validateToken(jwt, user)) {

                // Створюємо об’єкт автентифікації для Spring Security
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                user,                    // principal — сам об’єкт User
                                null,                    // credentials — не потрібні, бо вже є JWT
                                user.getAuthorities()    // ролі/права користувача
                        );

                // Додаємо деталі запиту (IP, sessionId тощо) — корисно для логування/аудиту
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Зберігаємо автентифікацію в SecurityContext — тепер @AuthenticationPrincipal працює!
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // У будь-якому випадку продовжуємо ланцюжок фільтрів
        filterChain.doFilter(request, response);
    }
}