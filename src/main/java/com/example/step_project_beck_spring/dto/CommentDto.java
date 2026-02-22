package com.example.step_project_beck_spring.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentDto {
    private UUID id;
    private String content;
    private UUID authorId;
    private String authorNickName;
    private UUID postId;
    private LocalDateTime createdAt;
    private int commentsCount;
}