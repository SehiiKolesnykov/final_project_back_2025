package com.example.step_project_beck_spring.dto;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LikeDto {
    private UUID postId;
    private UUID userId;
    private boolean liked;
    private long totalLikes;
}


