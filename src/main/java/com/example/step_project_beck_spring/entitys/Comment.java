package com.example.step_project_beck_spring.entitys;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Сутність коментаря до посту.*
 */
@Entity
@Table(
        name = "comments",
        indexes = {
                // Прискорює завантаження коментарів до конкретного поста
                @Index(name = "idx_comment_post_created", columnList = "post_id, created_at DESC"),
                // Додатково: швидкий пошук коментарів користувача
                @Index(name = "idx_comment_author", columnList = "author_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Comment {

    /**
     * Id коментаря.*
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    /**
     * Текст коментаря.
     * Максимум 500 символів — достатньо для нормального коментаря.
     */
    @Column(nullable = false, length = 500)
    private String content;

    /**
     * Автор коментаря (хто написав).
     * LAZY — завантажується тільки при потребі.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "author_id",
            nullable = false,
            updatable = false,
            foreignKey = @ForeignKey(name = "fk_comment_author")
    )
    private User author;

    /**
     * Пост, до якого залишено коментар.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "post_id",
            nullable = false,
            updatable = false,
            foreignKey = @ForeignKey(name = "fk_comment_post")
    )
    private Post post;

    /**
     * Дата і час створення коментаря.
     * Автоматично заповнюється при збереженні.
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}