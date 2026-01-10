package com.example.step_project_beck_spring.controller;

import com.example.step_project_beck_spring.dto.PostDto;
import com.example.step_project_beck_spring.request.CreatePostRequest;
import com.example.step_project_beck_spring.request.UpdatePostRequest;
import com.example.step_project_beck_spring.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST-контролер для роботи з постами
 * Базовий шлях: /api/posts
 */
@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    /**
     * Створює новий пост
     *
     * @param request DTO з даними для створення поста
     * @return створений пост у вигляді PostDto
     * @status 200 OK
     */
    @PostMapping
    public ResponseEntity<PostDto> createPost(@Valid @RequestBody CreatePostRequest request) {
        PostDto created = postService.createPost(request);
        return ResponseEntity.ok(created);
    }

    /**
     * Часткове оновлення існуючого поста (PATCH)
     *
     * @param id      ідентифікатор поста
     * @param request DTO з полями, які потрібно оновити
     * @return оновлений пост
     * @status 200 OK
     */
    @PatchMapping("/{id}")
    public ResponseEntity<PostDto> updatePost(
            @PathVariable UUID id,
            @Valid @RequestBody UpdatePostRequest request) {
        PostDto updated = postService.updatePost(id, request);
        return ResponseEntity.ok(updated);
    }

    /**
     * Видаляє пост за ідентифікатором
     *
     * @param id ідентифікатор поста для видалення
     * @status 204 No Content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable UUID id) {
        postService.deletePost(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Отримання стрічки постів (feed) поточного користувача
     * (зазвичай пости від людей, на яких підписаний + власні)
     *
     * @param page номер сторінки (починається з 0)
     * @param size розмір сторінки (кількість елементів)
     * @return сторінка з постами
     */
    @GetMapping("/feed")
    public ResponseEntity<Page<PostDto>> getFeed(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PostDto> feed = postService.getFeed(pageable);
        return ResponseEntity.ok(feed);
    }

    /**
     * Отримання рекомендованих постів для поточного користувача
     *
     * @param page номер сторінки
     * @param size кількість елементів на сторінці
     * @return сторінка з рекомендованими постами
     */
    @GetMapping("/recommended")
    public ResponseEntity<Page<PostDto>> getRecommended(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PostDto> recommended = postService.getRecommended(pageable);
        return ResponseEntity.ok(recommended);
    }

    /**
     * Отримання одного конкретного поста за його ідентифікатором
     *
     * @param id ідентифікатор поста
     * @return знайдений пост
     * @status 200 OK
     */
    @GetMapping("/{id}")
    public ResponseEntity<PostDto> getPostById(@PathVariable UUID id) {
        PostDto post = postService.getPostById(id);
        return ResponseEntity.ok(post);
    }

    /**
     * Отримання всіх постів конкретного користувача
     *
     * @param userId ідентифікатор автора постів
     * @param page   номер сторінки
     * @param size   розмір сторінки
     * @return сторінка з постами вказаного користувача
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<PostDto>> getUserPosts(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PostDto> posts = postService.getUserPosts(userId, pageable);
        return ResponseEntity.ok(posts);
    }

    /**
     * Додає пост до збережених (закладок) поточного користувача
     *
     * @param id ідентифікатор поста
     * @status 200 OK
     */
    @PostMapping("/{id}/save")
    public ResponseEntity<Void> savePost(@PathVariable UUID id) {
        postService.savePost(id);
        return ResponseEntity.ok().build();
    }

    /**
     * Видаляє пост зі збережених (закладок) поточного користувача
     *
     * @param id ідентифікатор поста
     * @status 204 No Content
     */
    @DeleteMapping("/{id}/save")
    public ResponseEntity<Void> unsavePost(@PathVariable UUID id) {
        postService.unsavePost(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Отримання списку всіх збережених постів поточного користувача
     *
     * @param page номер сторінки
     * @param size кількість елементів на сторінці
     * @return сторінка зі збереженими постами
     */
    @GetMapping("/saved")
    public ResponseEntity<Page<PostDto>> getSavedPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PostDto> saved = postService.getSavedPosts(pageable);
        return ResponseEntity.ok(saved);
    }
}