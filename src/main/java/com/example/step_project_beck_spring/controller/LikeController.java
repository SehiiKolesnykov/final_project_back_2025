package com.example.step_project_beck_spring.controller;

import com.example.step_project_beck_spring.dto.LikeDto;
import com.example.step_project_beck_spring.request.LikeRequest;
import com.example.step_project_beck_spring.service.LikeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/likes")
@RequiredArgsConstructor
public class LikeController {

    private final LikeService likeService;

    /**
     *  пeремикання лайку (toggle).
     * Якщо користувач вже лайкнув пост то лайк видаляється.
     * Якщо ще не лайкнув  лайк додається.
     * Повертає DTO з інформацією про стан лайку та загальну кількість лайків.
     */
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
    @GetMapping("/{postId}/count")
    public ResponseEntity<Long> countLikes(@PathVariable UUID postId) {
        return ResponseEntity.ok(likeService.countLikes(postId));
    }
}




