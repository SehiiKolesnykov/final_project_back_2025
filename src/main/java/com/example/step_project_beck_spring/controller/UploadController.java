package com.example.step_project_beck_spring.controller;

import com.example.step_project_beck_spring.entities.User;
import com.example.step_project_beck_spring.service.UploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

/**
 * Контролер для роботи з завантаженням файлів.
 * Всі ендпоінти — приватні, вимагають авторизацію через JWT.
 * Ніколи не приймає файли напряму — тільки видає підписи та видаляє.
 */
@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
public class UploadController {

    private final UploadService uploadService;

    /**
     * Повертає підпис для завантаження зображення.
     * type: avatar | background | post
     */
    @GetMapping("/signature/{type}")
    public ResponseEntity<Map<String, Object>> getSignature(
            @PathVariable String type,
            @AuthenticationPrincipal User principal) { // отримуємо авторизованого користувача

        String userId = principal.getId().toString();

        String folder = switch (type) {
            case "avatar"      -> "users/avatars/" + userId;
            case "background"  -> "users/backgrounds/" + userId;
            case "post"        -> "posts/" + userId;
            default -> throw new IllegalArgumentException("Invalid type: " + type);
        };

        long timestamp = System.currentTimeMillis() / 1000L;
        Map<String, Object> response = uploadService.generateSignature(folder, timestamp);
        return ResponseEntity.ok(response);
    }

    /**
     * Видаляє конкретне зображення з Cloudinary.
     */
    @DeleteMapping("/image")
    public ResponseEntity<Void> deleteImage(
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal User principal) {

        String publicId = body.get("publicId");
        uploadService.deleteImage(publicId, principal.getId());

        return ResponseEntity.ok().build();
    }

    /**
     * Витягує ID користувача з Principal.
     * Зараз — заглушка. В ідеалі — додати userId в JWT при логіні:
     * claims.put("userId", user.getId().toString());
     * І витягувати через JwtService.
     */
    private String getUserIdFromPrincipal(Principal principal) {
        // TODO: Замінити на реальне отримання з JWT claims
        // Приклад: return jwtService.getUserIdFromToken(...);
        return principal.getName();
    }
}