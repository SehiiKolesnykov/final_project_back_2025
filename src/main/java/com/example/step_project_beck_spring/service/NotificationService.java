package com.example.step_project_beck_spring.service;

import com.example.step_project_beck_spring.dto.NotificationDto;
import com.example.step_project_beck_spring.dto.NotificationPageDto;
import com.example.step_project_beck_spring.entities.Notification;
import com.example.step_project_beck_spring.entities.Post;
import com.example.step_project_beck_spring.entities.User;
import com.example.step_project_beck_spring.enums.NotificationType;
import com.example.step_project_beck_spring.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationWebSocketService notificationWebSocketService;

    /**
     * Універсальний метод для створення будь-якого сповіщення
     * Використовується для всіх типів: NEW_POST, LIKE, COMMENT, FOLLOW, MESSAGE
     */
    @Transactional
    public Notification createNotification(
            User recipient,
            User actor,                // хто викликав дію (той, хто лайкнув, написав, підписався тощо)
            NotificationType type,
            UUID relatedEntityId,      // ID поста, коментаря, повідомлення, користувача тощо
            String message,
            String link) {

        Notification notification = Notification.builder()
                .type(type)
                .recipient(recipient)
                .relatedEntityId(relatedEntityId)
                .message(message)
                .link(link)
                .isRead(false)
                .build();

        Notification saved = notificationRepository.save(notification);
        log.info("Створено сповіщення: id={}, type={}, recipient={}, message={}",
                saved.getId(), type, recipient.getEmail(), message);

        // Відправляємо через WebSocket
        NotificationDto dto = toDto(saved);
        notificationWebSocketService.sendNotificationToUser(recipient.getId(), dto);

        return saved;
    }

    // ──── Залишкові методи для сумісності (можна видалити після повного переходу) ────

    @Deprecated
    public void createNewPostNotification(User recipient, Post post) {
        createNotification(
                recipient,
                post.getAuthor(),
                NotificationType.NEW_POST,
                post.getId(),
                post.getAuthor().getNickName() + " опублікував(-ла) новий пост",
                "/posts/" + post.getId()
        );
    }

    // ──── Отримання сповіщень користувача з пагінацією ────
    @Transactional(readOnly = true)
    public NotificationPageDto getNotifications(UUID recipientId, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size);
        Page<Notification> notificationPage =
                notificationRepository.findByRecipientIdOrderByCreatedAtDesc(recipientId, pageable);

        return new NotificationPageDto(
                notificationPage.getContent().stream().map(this::toDto).toList(),
                notificationPage.getNumber(),
                notificationPage.getSize(),
                notificationPage.getTotalElements(),
                notificationPage.getTotalPages(),
                notificationPage.hasNext()
        );
    }

    // ──── Позначити одне сповіщення як прочитане ────
    @Transactional
    public void markAsRead(UUID notificationId, UUID recipientId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Сповіщення не знайдено"));

        if (!notification.getRecipient().getId().equals(recipientId)) {
            throw new IllegalArgumentException("Немає доступу до цього сповіщення");
        }

        if (!notification.isRead()) {
            notification.setRead(true);
            notificationRepository.save(notification);
            log.info("Сповіщення {} позначено як прочитане для користувача {}", notificationId, recipientId);
        }
    }

    // ──── Позначити всі сповіщення як прочитані ────
    @Transactional
    public void markAllAsRead(UUID recipientId) {
        // Для ефективності можна зробити bulk update, але поки що через сторінки
        PageRequest pageRequest = PageRequest.of(0, 1000);
        Page<Notification> page = notificationRepository.findByRecipientIdOrderByCreatedAtDesc(recipientId, pageRequest);

        page.forEach(n -> {
            if (!n.isRead()) {
                n.setRead(true);
            }
        });

        notificationRepository.saveAll(page.getContent());
        log.info("Всі сповіщення позначено як прочитані для користувача {}", recipientId);
    }

    // ──── Кількість непрочитаних сповіщень ────
    @Transactional(readOnly = true)
    public long countUnread(UUID recipientId) {
        long count = notificationRepository.countByRecipientIdAndIsReadFalse(recipientId);
        log.debug("Непрочитаних сповіщень для {}: {}", recipientId, count);
        return count;
    }

    // ──── Видалення одного сповіщення ────
    @Transactional
    public void deleteNotification(UUID notificationId, UUID recipientId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Сповіщення не знайдено"));

        if (!notification.getRecipient().getId().equals(recipientId)) {
            throw new IllegalArgumentException("Немає доступу");
        }

        notificationRepository.delete(notification);
        log.info("Сповіщення {} видалено для користувача {}", notificationId, recipientId);
    }

    // ──── Маппер Notification → DTO ────
    private NotificationDto toDto(Notification notification) {
        return new NotificationDto(
                notification.getId(),
                notification.getType(),
                notification.getMessage(),
                notification.getLink(),
                notification.isRead(),
                notification.getCreatedAt()
        );
    }
}