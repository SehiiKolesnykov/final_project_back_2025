package com.example.step_project_beck_spring.dto;

import java.util.UUID;

/**
 * Коротка інформація про користувача для списків фоловерів.
 */
public record UserSummaryDto(
        UUID id,
        String firstName,
        String lastName,
        String avatarUrl,
        String nickName,
        String aboutMe,
        Boolean isFollowing
) {
    // Конструктор з 5 параметрами
    public UserSummaryDto(UUID id, String firstName, String lastName, String avatarUrl, String nickName, String aboutMe) {
        this(id, firstName, lastName, avatarUrl, nickName, aboutMe, null);
    }
}