package com.example.step_project_beck_spring.entities;

import com.example.step_project_beck_spring.enums.NotificationType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Сутність сповіщення*
 * Підтримує типізацію (LIKE, COMMENT, FOLLOW тощо) та посилання на пов'язану сутність.
 */
@Entity
@Table(
        name = "notifications",
        indexes = {
                @Index(name = "idx_notification_recipient_created", columnList = "recipient_id, created_at DESC"),
                @Index(name = "idx_notification_recipient_read", columnList = "recipient_id, is_read"),
                @Index(name = "idx_notification_type", columnList = "type"),
                @Index(name = "idx_notification_related", columnList = "related_entity_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    /**
     * Тип сповіщення
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private NotificationType type;

    /**
     * ID пов'язаної сутності (пост, коментар, користувач тощо).
     * Наприклад:
     * - LIKE → ID поста
     * - COMMENT → ID коментаря
     * - FOLLOW → ID користувача, який підписався
     */
    @Column(name = "related_entity_id")
    private UUID relatedEntityId;

    /**
     * Текст сповіщення (для відображення)
     */
    @Column(name = "message", nullable = false, length = 200)
    private String message;

    /**
     * Готове посилання для переходу (опціонально — можна генерувати на фронті)
     */
    @Column(name = "link", length = 500)
    private String link;

    /**
     * Чи прочитано сповіщення
     */
    @Column(name = "is_read", nullable = false)
    private boolean isRead = false;

    /**
     * Отримувач сповіщення
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "recipient_id",
            nullable = false,
            updatable = false,
            foreignKey = @ForeignKey(name = "fk_notification_recipient")
    )
    private User recipient;

    /**
     * Коли створено
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
