package com.example.step_project_beck_spring.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.step_project_beck_spring.config.CloudinaryConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

/**
 * Сервіс для безпечної роботи з Cloudinary.
 *
 * Основні задачі:
 * 1. Генерувати підпис (signature) для прямого завантаження з фронтенду
 * 2. Безпечне видалення зображень — тільки власних!
 *
 * Важливо: api_secret використовується ТІЛЬКИ тут, на бекенді. На клієнт віддаємо лише підпис.
 */
@Service
@RequiredArgsConstructor
public class UploadService {

    // Підключений Cloudinary-клієнт (бін з CloudinaryConfig)
    private final Cloudinary cloudinary;

    // Потрібен тільки для отримання cloudName, apiKey та apiSecret
    private final CloudinaryConfig cloudinaryConfig;

    /**
     * Генерує підпис для прямого завантаження на Cloudinary.
     *
     * @param folder     папка в Cloudinary (наприклад users/avatars/123e4567-...)
     * @param timestamp  поточний Unix-timestamp (в секундах)
     * @return мапа з усіма даними, які потрібні фронтенду для завантаження
     */
    public Map<String, Object> generateSignature(String folder, long timestamp) {

        // Параметри, які підписуємо (timestamp + folder — обов’язкові для безпеки)
        Map<String, Object> paramsToSign = Map.of(
                "timestamp", timestamp,
                "folder", folder
        );

        // Генеруємо підпис за допомогою apiSecret (секрет залишається тільки на сервері!)
        String signature = cloudinary.apiSignRequest(paramsToSign, cloudinaryConfig.getApiSecret(), 1);

        // Повертаємо все, що потрібно фронтенду для Cloudinary upload widget
        return Map.of(
                "cloudName", cloudinaryConfig.getCloudName(),
                "apiKey",    cloudinaryConfig.getApiKey(),
                "signature", signature,
                "timestamp", timestamp,
                "folder",    folder
        );
    }

    /**
     * Видаляє зображення з Cloudinary.
     * Перевіряє, що publicId містить UUID поточного користувача — інакше 403.
     *
     * @param publicId       повний public_id зображення (наприклад users/avatars/123e4567-.../file.jpg)
     * @param currentUserId  ID користувача, який робить запит
     */
    public void deleteImage(String publicId, UUID currentUserId) {
        if (publicId == null || publicId.isBlank()) {
            throw new IllegalArgumentException("publicId is required");
        }

        // Витягуємо UUID користувача з шляху publicId
        String userIdFromPath = extractUserIdFromPublicId(publicId);

        // Якщо в шляху немає UUID або він не збігається з поточним користувачем — блокуємо
        if (userIdFromPath == null || !userIdFromPath.equals(currentUserId.toString())) {
            throw new AccessDeniedException("You can only delete your own images!");
        }

        // Виклик до Cloudinary для видалення
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete image from Cloudinary", e);
        }
    }

    /**
     * Шукає UUID (у форматі xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx) у частині public_id.
     * Використовується для перевірки власника зображення.
     */
    private String extractUserIdFromPublicId(String publicId) {
        String[] parts = publicId.split("/");
        for (String part : parts) {
            // Регулярка перевіряє саме формат UUID
            if (part.matches("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}")) {
                return part;
            }
        }
        return null;
    }
}