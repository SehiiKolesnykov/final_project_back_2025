package com.example.step_project_beck_spring.controller;

import com.example.step_project_beck_spring.service.CurrentUserService;
import com.example.step_project_beck_spring.service.UploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.Map;
import java.util.UUID;

/**
 * Контролер для безпечного завантаження файлів через Cloudinary.
 * Важливо: самі файли НІКОЛИ не проходять через наш бекенд!
 * Фронтенд отримує підпис (signature) → завантажує напряму в Cloudinary → ми лише зберігаємо public_id.
 *
 * Всі ендпоінти захищені JWT (потрібна автентифікація).
 */
@RestController                     // Повертає JSON
@RequestMapping("/api/upload")
@RequiredArgsConstructor
@Tag(name = "Upload", description = "API для завантаження та видалення зображень через Cloudinary")
public class UploadController {

    // Сервіс, який генерує підписи та видаляє зображення у Cloudinary
    private final UploadService uploadService;

    // Сервіс для отримання ID поточного користувача з JWT (без зайвого витягування User-об’єкта)
    private final CurrentUserService currentUserService;

    /**
     * GET /api/upload/signature/{type}
     *
     * Повертає підпис (signature) + timestamp для прямого завантаження на Cloudinary.
     * Типи:
     *  - avatar      → папка users/avatars/{userId}
     *  - background  → папка users/backgrounds/{userId}
     *  - post        → папка posts/{userId}
     */
    @Operation(summary = "Отримати підпис для завантаження", description = "Генерує підпис для прямого завантаження файлів на Cloudinary")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Підпис успішно згенеровано"),
            @ApiResponse(responseCode = "400", description = "Невірний тип завантаження")
    })
    @GetMapping("/signature/{type}")
    public ResponseEntity<Map<String, Object>> getSignature(@PathVariable String type) {

        // Отримуємо ID авторизованого користувача з JWT (без @AuthenticationPrincipal)
        UUID userId = currentUserService.getCurrentUserId();

        // Визначаємо папку в Cloudinary залежно від типу завантаження
        String folder = switch (type) {
            case "avatar"      -> "users/avatars/" + userId;
            case "background"  -> "users/backgrounds/" + userId;
            case "post"        -> "posts/" + userId;
            default -> throw new IllegalArgumentException("Invalid type: " + type);
        };

        // Timestamp потрібен Cloudinary для підпису (в секундах)
        long timestamp = System.currentTimeMillis() / 1000L;

        // Генеруємо підпис та інші параметри через UploadService
        Map<String, Object> response = uploadService.generateSignature(folder, timestamp);

        return ResponseEntity.ok(response);
    }

    /**
     * DELETE /api/upload/image
     *
     * Видаляє зображення з Cloudinary за public_id.
     *
     * Очікує JSON:
     * { "publicId": "users/avatars/123e4567-e89b-12d3-a456-426614174000/somefile" }
     */
    @Operation(summary = "Видалити зображення", description = "Видаляє зображення з Cloudinary за public_id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Зображення успішно видалено"),
            @ApiResponse(responseCode = "403", description = "Немає прав на видалення цього зображення")
    })
    @DeleteMapping("/image")
    public ResponseEntity<Void> deleteImage(@RequestBody Map<String, String> body) {

        String publicId = body.get("publicId");

        // Отримуємо ID поточного користувача для перевірки прав
        UUID currentUserId = currentUserService.getCurrentUserId();

        // Видаляємо зображення (з перевіркою, чи належить воно цьому користувачу)
        uploadService.deleteImage(publicId, currentUserId);

        return ResponseEntity.ok().build(); // 200 OK без тіла
    }
}