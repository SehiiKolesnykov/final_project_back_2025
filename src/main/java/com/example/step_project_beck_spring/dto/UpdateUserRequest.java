package com.example.step_project_beck_spring.dto;

import java.time.LocalDate;

public record UpdateUserRequest(
        String firstName,
        String lastName,
        String avatarUrl,
        String backgroundImgUrl,
        LocalDate birthDate,
        String aboutMe
) {}