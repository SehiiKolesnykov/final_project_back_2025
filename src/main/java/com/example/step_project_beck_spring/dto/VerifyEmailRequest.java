package com.example.step_project_beck_spring.dto;

public record VerifyEmailRequest(
        String email,
        String verificationCode
) {}