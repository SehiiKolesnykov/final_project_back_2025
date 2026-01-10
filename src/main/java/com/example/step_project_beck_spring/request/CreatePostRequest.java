package com.example.step_project_beck_spring.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO-запит для створення нового поста.
 * Містить обов'язковий текстовий вміст та опціональне зображення.
 * Використовується для валідації вхідних даних у контролері.
 */
public class CreatePostRequest {

    /**
     * Текстовий вміст поста (обов'язкове поле).
     * Валідація:
     * - Не може бути порожнім або null (@NotBlank)
     * - Максимальна довжина: 280 символів (як у Twitter/X)
     */
    @NotBlank(message = "Content is required")
    @Size(max = 280, message = "Content must be at most 280 characters")
    private String content;

    /**
     * URL зображення, яке прикріплюється до поста (опціональне поле).
     * Якщо не надано, пост буде без зображення.
     * Рекомендується використовувати прямі посилання на файли (наприклад, з S3 або Cloudinary).
     */
    private String imageUrl;

    /**
     * Геттер для вмісту поста
     *
     * @return текстовий вміст
     */
    public String getContent() {
        return content;
    }

    /**
     * Сеттер для вмісту поста
     *
     * @param content новий вміст поста
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * Геттер для URL зображення
     *
     * @return URL зображення або null, якщо не встановлено
     */
    public String getImageUrl() {
        return imageUrl;
    }

    /**
     * Сеттер для URL зображення
     *
     * @param imageUrl URL зображення
     */
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}