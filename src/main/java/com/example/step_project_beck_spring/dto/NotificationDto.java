package com.example.step_project_beck_spring.dto;

import com.example.step_project_beck_spring.enums.NotificationType;
import java.time.LocalDateTime;
import java.util.UUID;

//DTO для одного сповіщення для відображення списку нотифікацій на фронті.
public record NotificationDto(
        UUID id,
        NotificationType type,
        String message,
        String link,
        boolean isRead,
        LocalDateTime createdAt
) {}


