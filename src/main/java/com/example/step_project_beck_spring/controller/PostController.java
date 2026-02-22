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
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @PostMapping
    public ResponseEntity<PostDto> createPost(@Valid @RequestBody CreatePostRequest request) {
        return ResponseEntity.ok(postService.createPost(request));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<PostDto> updatePost(
            @PathVariable UUID id,
            @Valid @RequestBody UpdatePostRequest request) {
        return ResponseEntity.ok(postService.updatePost(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable UUID id) {
        postService.deletePost(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/feed")
    public ResponseEntity<Page<PostDto>> getFeed(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "newest") String sort) {
        Pageable pageable = getPageable(page, size, sort);
        return ResponseEntity.ok(postService.getFeed(pageable));
    }

    @GetMapping("/recommended")
    public ResponseEntity<Page<PostDto>> getRecommended(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "newest") String sort) {
        Pageable pageable = getPageable(page, size, sort);
        return ResponseEntity.ok(postService.getRecommended(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostDto> getPostById(@PathVariable UUID id) {
        return ResponseEntity.ok(postService.getPostById(id));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<PostDto>> getUserPosts(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "newest") String sort) {
        Pageable pageable = getPageable(page, size, sort);
        return ResponseEntity.ok(postService.getUserPosts(userId, pageable));
    }

    @PostMapping("/{id}/save")
    public ResponseEntity<Void> savePost(@PathVariable UUID id) {
        postService.savePost(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}/save")
    public ResponseEntity<Void> unsavePost(@PathVariable UUID id) {
        postService.unsavePost(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/saved")
    public ResponseEntity<Page<PostDto>> getSavedPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "newest") String sort) {
        Pageable pageable = getPageable(page, size, sort);
        return ResponseEntity.ok(postService.getSavedPosts(pageable));
    }

    // ──── Допоміжний метод для створення Pageable з підтримкою сортування ────
    private Pageable getPageable(int page, int size, String sort) {
        Sort sortObj;
        switch (sort.toLowerCase()) {
            case "oldest":
                sortObj = Sort.by(Sort.Direction.ASC, "createdAt");
                break;
            case "likes-desc":
                sortObj = Sort.by(Sort.Direction.DESC, "likesCount")
                        .and(Sort.by(Sort.Direction.DESC, "createdAt"));
                break;
            case "likes-asc":
                sortObj = Sort.by(Sort.Direction.ASC, "likesCount")
                        .and(Sort.by(Sort.Direction.DESC, "createdAt"));
                break;
            case "comments-desc":
                sortObj = Sort.by(Sort.Direction.DESC, "commentsCount")
                        .and(Sort.by(Sort.Direction.DESC, "createdAt"));
                break;
            case "comments-asc":
                sortObj = Sort.by(Sort.Direction.ASC, "commentsCount")
                        .and(Sort.by(Sort.Direction.DESC, "createdAt"));
                break;
            case "newest":
            default:
                sortObj = Sort.by(Sort.Direction.DESC, "createdAt");
                break;
        }

        return PageRequest.of(page, size, sortObj);
    }
}