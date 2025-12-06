package com.example.step_project_beck_spring.request;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public class LikeRequest {

    @NotNull
    private UUID userId;

    public UUID getUserId() {
        return userId;
    }
    public void setUserId(UUID userId) {
        this.userId = userId;
    }
}


