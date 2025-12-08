package com.example.step_project_beck_spring.service;

import com.example.step_project_beck_spring.entities.User;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import java.util.UUID;

/**
 * Утилітний сервіс для зручного отримання поточного автентифікованого користувача.
 *
 * Використовується в інших сервісах, де потрібно швидко взяти
 * ID або повний об’єкт User з JWT (з SecurityContext).
 *
 * Дякуючи JwtAuthenticationFilter та AuthService, у SecurityContext завжди лежить
 * саме об’єкт User, тому можна безпечно брати.
 */
@Service
public class CurrentUserService {

    /**
     * Повертає ID поточного користувача.
     *
     * Кидає AccessDeniedException (403), якщо користувач не автентифікований
     * або в контексті щось інше (наприклад анонімний користувач).
     *
     * @return UUID поточного користувача
     */
    public UUID getCurrentUserId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();

        // Перевіряємо, що автентифікація є і principal — саме наш User
        if (auth == null ||
                auth.getPrincipal() == null ||
                !(auth.getPrincipal() instanceof User user)) {
            throw new AccessDeniedException("Not authenticated");
        }

        return user.getId();
    }

    // Тепер у будь-якому місці можна просто написати:
    // UUID userId = currentUserService.getCurrentUserId();
    // без @AuthenticationPrincipal та зайвих параметрів у контролері

    /**
     * Повертає повний об’єкт User поточного користувача.
     *
     * @return об’єкт User з бази
     */
    public User getCurrentUser() {
        var auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null ||
                auth.getPrincipal() == null ||
                !(auth.getPrincipal() instanceof User user)) {
            throw new AccessDeniedException("Not authenticated");
        }

        return user;
    }
}