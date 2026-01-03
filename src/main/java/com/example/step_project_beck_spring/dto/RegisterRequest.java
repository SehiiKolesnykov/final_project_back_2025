package com.example.step_project_beck_spring.dto;

import java.time.LocalDate;

public record RegisterRequest(
        String firstName,
        String lastName,
        String email,
        String password,
        LocalDate birthDate
) {}