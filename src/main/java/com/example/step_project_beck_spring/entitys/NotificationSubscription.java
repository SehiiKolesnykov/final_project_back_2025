package com.example.step_project_beck_spring.entitys;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Окрема підписка на сповіщення про нові пости користувача.
 * Незалежна від звичайної підписки (Follow).
 * Користувач може бути followed, але НЕ отримувати сповіщення — і навпаки.
 */
@Entity
@Table(name = "notification_subscriptions",
        uniqueConstraints = @UniqueConstraint(columnNames = {"subscriber_id", "target_user_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationSubscription {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /** Хто хоче отримувати сповіщення */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscriber_id", nullable = false)
    private User subscriber;

    /** Про чиї нові пости сповіщати */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_user_id", nullable = false)
    private User targetUser;

    /** Коли увімкнув сповіщення */
    private LocalDateTime subscribedAt = LocalDateTime.now();

    /** Додатково: можна зробити вибіркові сповіщення (всі пости / тільки важливі тощо) */
    private boolean notifyOnAllPosts = true;
}
