package com.example.step_project_beck_spring.service;

import com.example.step_project_beck_spring.dto.NotificationDto;
import com.example.step_project_beck_spring.dto.NotificationPageDto;
import com.example.step_project_beck_spring.entities.Notification;
import com.example.step_project_beck_spring.entities.Post;
import com.example.step_project_beck_spring.entities.User;
import com.example.step_project_beck_spring.enums.NotificationType;
import com.example.step_project_beck_spring.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final NotificationWebSocketService notificationWebSocketService;

    //Створює сповіщення про новий пост та пушить його по WebSocket.
    @Transactional
    public void createNewPostNotification(User recipient, Post post) {
        Notification notification = Notification.builder()
                .type(NotificationType.NEW_POST)
                .recipient(recipient)
                .relatedEntityId(post.getId())
                .message(post.getAuthor().getFirstName() + " " + post.getAuthor().getLastName()
                        + " опублікував(-ла) новий пост")
                .link("/posts/" + post.getId())
                .build();
        Notification saved = notificationRepository.save(notification);

        // Пушимо через WebSocket (як DTO)
        NotificationDto dto = toDto(saved);
        notificationWebSocketService.sendNotificationToUser(recipient.getId(), dto);
    }

    //Отримати сторінку сповіщень для користувача.
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

    //Позначити конкретну нотифікацію як прочитану.
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
        }
    }

    //Позначити всі сповіщення користувача як прочитані.
    @Transactional
    public void markAllAsRead(UUID recipientId) {
        PageRequest pageRequest = PageRequest.of(0, 1000); // якщо дуже багато — подумати про bulk update
        Page<Notification> page =
                notificationRepository.findByRecipientIdOrderByCreatedAtDesc(recipientId, pageRequest);
        page.forEach(n -> {
            if (!n.isRead()) {
                n.setRead(true);
            }
        });
        notificationRepository.saveAll(page.getContent());
    }

    //Кількість непрочитаних сповіщень.
    @Transactional(readOnly = true)
    public long countUnread(UUID recipientId) {
        return notificationRepository.countByRecipientIdAndIsReadFalse(recipientId);
    }

    //Маппер Notification -> NotificationDto.
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

    // видалення сповіщень
    @Transactional
    public void deleteNotification(UUID notificationId, UUID recipientId) {
        Notification n = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Сповіщення не знайдено"));
        if (!n.getRecipient().getId().equals(recipientId)) {
            throw new IllegalArgumentException("Немає доступу");
        }
        notificationRepository.delete(n);
    }
}




