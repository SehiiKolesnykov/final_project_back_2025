package com.example.step_project_beck_spring.controller;

import com.example.step_project_beck_spring.dto.CommentDto;
import com.example.step_project_beck_spring.request.CreateCommentRequest;
import com.example.step_project_beck_spring.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    /**
     * Метод для додавання нового коментаря до певного поста.
     * postId поста до якого додається коментар.
     * request DTO з даними (userId) та текстом коментаря.
     * Повертає створений коментар у вигляді CommentDto.
     */
    @PostMapping("/{postId}")
    public ResponseEntity<CommentDto> addComment(
            @PathVariable UUID postId,
            @Valid @RequestBody CreateCommentRequest request) {
        return ResponseEntity.ok(
                commentService.addComment(postId, request.getUserId(), request.getContent())
        );
    }
    /**
     * Метод отримання всіх коментів до конкретного поста.
     * Повертаємо список коментарів (CommentDto) спочатку нові потім старі.
     */
    @GetMapping("/{postId}")
    public ResponseEntity<List<CommentDto>> getComments(@PathVariable UUID postId) {
        return ResponseEntity.ok(commentService.getComments(postId));
    }
}



