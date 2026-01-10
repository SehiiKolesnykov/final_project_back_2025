package com.example.step_project_beck_spring.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO для передачі даних про пост між шарами програми
 * Використовується для відповідей API та представлення поста клієнту
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostDto {

    /** Унікальний ідентифікатор поста */
    private UUID id;

    /** Основний текстовий вміст поста */
    private String content;

    /** URL зображення, прикріпленого до поста (якщо є) */
    private String imageUrl;

    /** Коротка інформація про автора поста */
    private UserSummaryDto author;

    /** Дата та час створення поста */
    private LocalDateTime createdAt;

    /** Кількість лайків під постом */
    private int likesCount;

    /** Кількість коментарів до поста */
    private int commentsCount;

    /** Кількість репостів (поширень) поста */
    private int repostsCount;

    /** Кількість цитат цього поста (quote posts) */
    private int quotesCount;
}