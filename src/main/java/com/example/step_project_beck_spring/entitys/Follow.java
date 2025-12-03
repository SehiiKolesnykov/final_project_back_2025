package com.example.step_project_beck_spring.entitys;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Проміжна сутність для підписки.
 * Дозволяє легко отримати:
 * - На кого підписався користувач
 * - Хто підписався на користувача
 * - Час підписки
 * - Унікальність (один не може підписатись двічі)
 */
@Entity
@Table(name = "follows",
        uniqueConstraints = @UniqueConstraint(columnNames = {"follower_id", "following_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Follow {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /** Хто підписався */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "follower_id", nullable = false)
    private User follower;

    /** На кого підписався */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "following_id", nullable = false)
    private User following;

    /** Коли підписався — для сортування стрічки */
    private LocalDateTime followedAt = LocalDateTime.now();
}
