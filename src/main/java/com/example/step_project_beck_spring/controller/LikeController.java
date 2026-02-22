package com.example.step_project_beck_spring.controller;

import com.example.step_project_beck_spring.dto.LikeDto;
import com.example.step_project_beck_spring.request.LikeRequest;
import com.example.step_project_beck_spring.service.LikeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.UUID;

@RestController
@RequestMapping("/api/likes")
@RequiredArgsConstructor
@Tag(name = "Likes", description = "API для управління лайками на постах")
public class LikeController {

    private final LikeService likeService;

    /**
     *  пeремикання лайку (toggle).
     * Якщо користувач вже лайкнув пост то лайк видаляється.
     * Якщо ще не лайкнув  лайк додається.
     * Повертає DTO з інформацією про стан лайку та загальну кількість лайків.
     */
    @Operation(summary = "Перемкнути лайк", description = "Додає або видаляє лайк на пості")
    @ApiResponse(responseCode = "200", description = "Лайк успішно перемкнуто")
    @PostMapping("/{postId}")
    public ResponseEntity<LikeDto> toggleLike(
            @PathVariable UUID postId,
            @Valid @RequestBody LikeRequest request) {
        return ResponseEntity.ok(likeService.toggleLike(postId, request.getUserId()));
    }

    /**
     * Метод для отримання кількості лайків конкретного поста.
     * Використовується для відображення статистики (наприклад, "цей пост має 15 лайків").
     */
    @Operation(summary = "Отримати кількість лайків", description = "Повертає кількість лайків для конкретного поста")
    @ApiResponse(responseCode = "200", description = "Кількість лайків успішно отримано")
    @GetMapping("/{postId}/count")
    public ResponseEntity<Long> countLikes(@PathVariable UUID postId) {
        return ResponseEntity.ok(likeService.countLikes(postId));
    }
}




