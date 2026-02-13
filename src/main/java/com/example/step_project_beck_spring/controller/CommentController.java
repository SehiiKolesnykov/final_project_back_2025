package com.example.step_project_beck_spring.controller;

import com.example.step_project_beck_spring.dto.CommentDto;
import com.example.step_project_beck_spring.request.CreateCommentRequest;
import com.example.step_project_beck_spring.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
@Tag(name = "Comments", description = "API для управління коментарями до постів")
public class CommentController {

    private final CommentService commentService;

    /**
     * Метод для додавання нового коментаря до певного поста.
     * postId поста до якого додається коментар.
     * request DTO з даними (userId) та текстом коментаря.
     * Повертає створений коментар у вигляді CommentDto.
     */
    @Operation(summary = "Додати коментар", description = "Створює новий коментар до вказаного поста")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Коментар успішно створено"),
            @ApiResponse(responseCode = "400", description = "Невірні дані коментаря"),
            @ApiResponse(responseCode = "404", description = "Пост не знайдено")
    })
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
    @Operation(summary = "Отримати коментарі поста", description = "Повертає всі коментарі до вказаного поста, відсортовані за датою (нові першими)")
    @ApiResponse(responseCode = "200", description = "Список коментарів успішно отримано")
    @GetMapping("/{postId}")
    public ResponseEntity<List<CommentDto>> getComments(@PathVariable UUID postId) {
        return ResponseEntity.ok(commentService.getComments(postId));
    }
}



