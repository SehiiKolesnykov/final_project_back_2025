package com.example.step_project_beck_spring.controller;

import com.example.step_project_beck_spring.dto.UpdateUserRequest;
import com.example.step_project_beck_spring.dto.UserPublicDTO;
import com.example.step_project_beck_spring.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;
import java.util.UUID;

/** REST-контролер для управління користувачами.
 * Дозволяє шукати інших користувачів та керувати власним профілем.
 */
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // ================== ПУБЛІЧНІ МЕТОДИ (ПОШУК ІНШИХ) ==================
    /**
     * GET /api/user/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserPublicDTO> getUserById(@PathVariable UUID id) {
        try {
            UserPublicDTO user = userService.getUserById(id);
            return ResponseEntity.ok(user); // 200 OK
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build(); // 404 Not Found
        }
    }

    /**
     * GET /api/user/by-email?email=test@example.com
     */
    @GetMapping("/by-email")
    public ResponseEntity<UserPublicDTO> getUserByEmail(@RequestParam String email) {
        try {
            UserPublicDTO user = userService.getUserByEmail(email);
            return ResponseEntity.ok(user); // 200 OK
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build(); // 404 Not Found
        }
    }

    // ================== ОСОБИСТІ МЕТОДИ (МIЙ ПРОФІЛЬ) ==================

    /**
     * GET /api/user/me
     * Отримує профіль ТОГО, ХТО ЗАРАЗ ЗАЛОГІНЕНИЙ.
     * Не потребує ID, бо бере його з токена.
     */
    @GetMapping("/me")
    public ResponseEntity<UserPublicDTO> getCurrentUser() {
        // Spring Security дістає email із JWT токена автоматично
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        // Використовуємо існуючий сервіс, щоб отримати DTO
        UserPublicDTO myProfile = userService.getUserByEmail(email);
        return ResponseEntity.ok(myProfile);
    }

    /**
     * PATCH /api/user/update
     * Оновлює дані поточного користувача (ім'я, фото і т.д.).
     * Приймає тільки ті поля, які треба змінити.
     */
    @PatchMapping("/update")
    public ResponseEntity<String> updateProfile(@RequestBody UpdateUserRequest request) {
        userService.updateProfile(request);
        return ResponseEntity.ok("Profile updated successfully / Профіль оновлено");
    }
}