package com.example.step_project_beck_spring.request;

import jakarta.validation.constraints.Size;

/**
 * DTO-запит для часткового оновлення (PATCH) існуючого поста.
 * Усі поля є опціональними — можна оновлювати лише частину даних.
 * Використовується для ендпоінта PATCH /api/posts/{id}
 */
public class UpdatePostRequest {

    /**
     * Новий текстовий вміст поста (опціонально).
     * Валідація:
     * - Якщо передано — максимальна довжина 280 символів
     * - Якщо не передано — вміст поста залишиться без змін
     */
    @Size(max = 280, message = "Content must be at most 280 characters")
    private String content;

    /**
     * Новий URL зображення для поста (опціонально).
     * <ul>
     *   <li>Якщо передано — замінить поточне зображення</li>
     *   <li>Якщо передано null — зображення буде видалено (залежить від реалізації сервісу)</li>
     *   <li>Якщо поле відсутнє в JSON — зображення залишиться без змін</li>
     * </ul>
     */
    private String imageUrl;

    /**
     * Отримує новий вміст поста
     *
     * @return вміст або null, якщо оновлення вмісту не передбачається
     */
    public String getContent() {
        return content;
    }

    /**
     * Встановлює новий вміст поста
     *
     * @param content новий текст поста (може бути null)
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * Отримує новий URL зображення
     *
     * @return URL зображення або null
     */
    public String getImageUrl() {
        return imageUrl;
    }

    /**
     * Встановлює новий URL зображення
     *
     * @param imageUrl URL зображення (може бути null)
     */
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}