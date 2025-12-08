package com.example.step_project_beck_spring.service;

import com.example.step_project_beck_spring.entities.User;
import com.example.step_project_beck_spring.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Сервіс для автентифікації користувача (логін).
 * Використовується в AuthController при звичайному вході (email + пароль).
 *
 * Основна задача:
 * 1. Перевірити коректність пароля через AuthenticationManager
 * 2. Завантажити повний об’єкт User з БД
 * 3. Встановити повноцінну автентифікацію в SecurityContext (з об’єктом User, а не просто email)
 * 4. Згенерувати та повернути JWT-токен
 */
@Service
public class AuthService {

    // Менеджер автентифікації (налаштований у SecurityConfig)
    @Autowired
    private AuthenticationManager authenticationManager;

    // Для пошуку користувача в базі після успішної перевірки пароля
    @Autowired
    private UserRepository userRepository;

    // Для створення JWT-токена
    @Autowired
    private JwtService jwtService;

    /**
     * Виконує логін і повертає JWT-токен.
     *
     * @param email      пошта користувача
     * @param password   пароль у чистому вигляді
     * @param rememberMe якщо true — токен з довшим терміном життя
     * @return готовий JWT-токен (String)
     */
    public String authenticateAndGetToken(String email, String password, boolean rememberMe) {

        // Spring Security перевіряє пароль (використовує PasswordEncoder, UserDetailsService тощо)
        // Якщо пароль неправильний — кидає BadCredentialsException
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );

        // Пароль правильний → завантажуємо повний об’єкт User з бази
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // Підміняємо principal в SecurityContext з email на повний об’єкт User.
        // Це важливо, щоб @AuthenticationPrincipal у контролерах повертав саме User, а не String.
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(
                        user,           // principal — повний об’єкт User
                        null,           // credentials — вже не потрібні
                        user.getAuthorities() // ролі та права
                );

        // Зберігаємо автентифікацію в поточному контексті (корисно для подальших сервісів у цьому запиті)
        SecurityContextHolder.getContext().setAuthentication(authToken);

        // Генеруємо JWT-токен. rememberMe впливає на термін дії токена (наприклад 7 днів vs 1 година)
        return jwtService.generateToken(user, rememberMe);
    }
}