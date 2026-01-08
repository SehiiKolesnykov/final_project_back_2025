package com.example.step_project_beck_spring.dto;
import java.util.List;

//DTO-обгортка для сторінки сповіщень з пагінацією.
public record NotificationPageDto(
        List<NotificationDto> notifications,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext
) {}


