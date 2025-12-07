package com.example.step_project_beck_spring.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.step_project_beck_spring.config.CloudinaryConfig;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

/**
 * Сервіс для роботи з Cloudinary.
 */
@Service
public class UploadService {

    private final Cloudinary cloudinary;
    private final CloudinaryConfig cloudinaryConfig;

    public UploadService(Cloudinary cloudinary, CloudinaryConfig cloudinaryConfig) {
        this.cloudinary = cloudinary;
        this.cloudinaryConfig = cloudinaryConfig;
    }

    /**
     * Генерує підпис для безпечного завантаження (signed upload).
     *
     * @param folder   повний шлях до папки (наприклад: users/avatars/550e8400-e29b-...)
     * @param timestamp таймштамп у секундах
     * @return мап з параметрами, які фронтенд передасть напряму Cloudinary
     */
    public Map<String, Object> generateSignature(String folder, long timestamp) {
        // Параметри, які будуть підписані
        Map<String, Object> paramsToSign = Map.of(
                "timestamp", timestamp,
                "folder", folder
        );

        // Підписуємо тільки на бекенді — api_secret ніколи не йде на фронт!
        String signature = cloudinary.apiSignRequest(paramsToSign, cloudinaryConfig.getApiSecret(), 1);

        // Повертаємо все необхідне для фронтенду
        return Map.of(
                "cloudName", cloudinaryConfig.getCloudName(),
                "apiKey", cloudinaryConfig.getApiKey(),
                "signature", signature,
                "timestamp", timestamp,
                "folder", folder
        );
    }

    /**
     * Видаляє зображення з Cloudinary за public_id.
     */
    public void deleteImage(String publicId, UUID currentUserId) {
        if (publicId == null || publicId.isBlank()) {
            throw new IllegalArgumentException("publicId cannot be empty");
        }

        // Отримуємо userId з папки в public_id
        String userIdFromPath = extractUserIdFromPublicId(publicId);

        if (userIdFromPath == null || !userIdFromPath.equals(currentUserId.toString())) {
            throw new AccessDeniedException("You can only delete your own images");
        }

        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete image from Cloudinary", e);
        }
    }

    /**
     * Витягує UUID користувача з public_id
     * Наприклад:
     * "users/avatars/f2b05697-b892-4f33-8a9d-598d9c2a6f11/abc123" → "f2b05697-b892-4f33-8a9d-598d9c2a6f11"
     */
    private String extractUserIdFromPublicId(String publicId) {
        // Шукаємо UUID після слешів
        String[] parts = publicId.split("/");
        for (String part : parts) {
            if (part.matches("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}")) {
                return part;
            }
        }
        return null;
    }
}