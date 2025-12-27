package com.example.step_project_beck_spring.repository;

import com.example.step_project_beck_spring.entities.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    //Отримати сповіщення користувача з пагінацією, нові першими.
    Page<Notification> findByRecipientIdOrderByCreatedAtDesc(UUID recipientId, Pageable pageable);

    //Кількість непрочитаних сповіщень для бейджика.
    long countByRecipientIdAndIsReadFalse(UUID recipientId);
}




