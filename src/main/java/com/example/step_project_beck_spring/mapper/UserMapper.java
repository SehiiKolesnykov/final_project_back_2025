package com.example.step_project_beck_spring.mapper;

import com.example.step_project_beck_spring.dto.UserSummaryDto;
import com.example.step_project_beck_spring.entities.User;
import org.springframework.stereotype.Component;

/**
 * Маппер для перетворення сутності User у DTO для списків.
 */
@Component
public class UserMapper {

    public UserSummaryDto toUserSummary(User user) {
        if (user == null) return null;
        return new UserSummaryDto(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getAvatarUrl()
        );
    }
}


