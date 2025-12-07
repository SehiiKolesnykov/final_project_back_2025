package com.example.step_project_beck_spring.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Сутність для збережених постів.
 */
@Entity
@Table(
        name = "saved_posts",
        indexes = {
                @Index(name = "idx_saved_posts_user", columnList = "user_id, saved_at DESC"),
                @Index(name = "idx_saved_posts_user_post", columnList = "user_id, post_id", unique = true)
        },
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"user_id", "post_id"},
                name = "uk_user_post_saved"
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SavedPost {

    /**
     * ID запису
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)  // ← ПРАВИЛЬНО!
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    /**
     * Користувач, який зберіг пост
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            updatable = false,
            foreignKey = @ForeignKey(name = "fk_saved_post_user")
    )
    private User user;

    /**
     * Пост, який було збережено
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "post_id",
            nullable = false,
            updatable = false,
            foreignKey = @ForeignKey(name = "fk_saved_post_post")
    )
    private Post post;

    /**
     * Коли користувач додав пост до "Обраного"
     */
    @CreationTimestamp
    @Column(name = "saved_at", nullable = false, updatable = false)
    private LocalDateTime savedAt;
}