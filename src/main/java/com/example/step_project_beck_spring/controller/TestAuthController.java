//package com.example.step_project_beck_spring.controller;
//
//import com.example.step_project_beck_spring.entities.User;
//import com.example.step_project_beck_spring.repository.UserRepository;
//import com.example.step_project_beck_spring.service.JwtService;
//import jakarta.servlet.http.HttpServletResponse;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseCookie;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//import java.util.UUID;
//
///**
// * Тестовий контролер тільки для розробки!
// * Дозволяє одним кліком залогінитись під тестовим користувачем і отримати JWT у куках.
// */
//@RestController
//@RequestMapping("/api/auth")
//public class TestAuthController {
//
//    // Репозиторій користувачів
//    @Autowired
//    private UserRepository userRepository;
//
//    // Для шифрування паролів (той самий, що й у SecurityConfig)
//    @Autowired
//    private PasswordEncoder passwordEncoder;
//
//    // Сервіс для створення JWT-токенів
//    @Autowired
//    private JwtService jwtService;
//
//    /**
//     * GET /api/auth/test-login
//     *
//     * Що робить:
//     * 1. Шукає користувача test@example.com
//     * 2. Якщо немає — створює його з паролем "testpassword" і зберігає в БД
//     * 3. Генерує JWT-токен (з флагом rememberMe = true)
//     * 4. Записує токен у HttpOnly-куку, щоб фронтенд не мав до нього доступу доступу через JS
//     * - secure = false — бо працюємо через http://localhost
//     */
//    @GetMapping("/test-login")
//    public ResponseEntity<?> testLogin(HttpServletResponse response) {
//
//        // Шукаємо тестового користувача або створюємо новий
//        User testUser = userRepository.findByEmail("test@example.com")
//                .orElseGet(() -> {
//                    User newUser = new User();
//                    newUser.setId(UUID.randomUUID());
//                    newUser.setEmail("test@example.com");
//                    // Пароль "testpassword" захешований BCrypt-ом
//                    newUser.setPassword(passwordEncoder.encode("testpassword"));
//                    newUser.setFirstName("Test");
//                    newUser.setLastName("User");
//                    newUser.setEmailVerified(true); // щоб не треба було верифікувати пошту
//                    return userRepository.save(newUser);
//                });
//
//        // Генеруємо JWT-токен.
//        String token = jwtService.generateToken(testUser, true);
//
//        // Створюємо безпечну HttpOnly-куку з токеном
//        ResponseCookie cookie = ResponseCookie.from("jwt", token)
//                .httpOnly(true)              // JS не може прочитати куку — захист від XSS
//                .secure(false)               // false тільки для localhost (http). У проді — true!
//                .sameSite("None")            // дозволяє крос-доменні запити з куками
//                .path("/")                   // кука доступна на всьому сайті
//                .maxAge(7 * 24 * 3600)       // 7 днів життя
//                .build();
//
//        // Додаємо куку в заголовок відповіді
//        response.addHeader("Set-Cookie", cookie.toString());
//
//        // Повертаємо просто повідомлення — фронтенду воно не потрібне, головне — кука
//        return ResponseEntity.ok("Test user logged in. JWT cookie set.");
//    }
//}