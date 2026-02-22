package com.example.step_project_beck_spring.controller;

import com.example.step_project_beck_spring.dto.UpdateUserRequest;
import com.example.step_project_beck_spring.dto.UserPublicDTO;
import com.example.step_project_beck_spring.entities.User;
import com.example.step_project_beck_spring.repository.FollowRepository;
import com.example.step_project_beck_spring.repository.UserRepository;
import com.example.step_project_beck_spring.service.CurrentUserService;
import com.example.step_project_beck_spring.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Tag(name = "Users", description = "API для управління користувачами та профілями")
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;
    private final FollowRepository followRepository;

    // ПУБЛІЧНІ МЕТОДИ (Пошук)
    @Operation(summary = "Отримати всіх користувачів", description = "Повертає список всіх користувачів")
    @ApiResponse(responseCode = "200", description = "Список користувачів успішно отримано")
    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<List<UserPublicDTO>> getAllUsers() {
        List<UserPublicDTO> users = userRepository.findAll().stream()
                .map(this::mapToPublicDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }

    @Operation(summary = "Пошук користувачів", description = "Шукає користувачів за ім'ям або прізвищем")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Результати пошуку успішно отримано"),
            @ApiResponse(responseCode = "400", description = "Порожній запит пошуку")
    })
    @GetMapping("/search")
    @Transactional(readOnly = true)
    public ResponseEntity<List<UserPublicDTO>> searchUsers(@RequestParam String q) {
        if (q == null || q.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        List<User> foundUsers = userRepository.findAll().stream()
                .filter(u -> (u.getFirstName() != null && u.getFirstName().toLowerCase().contains(q.toLowerCase())) ||
                        (u.getLastName() != null && u.getLastName().toLowerCase().contains(q.toLowerCase())))
                .collect(Collectors.toList());
        return ResponseEntity.ok(foundUsers.stream().map(this::mapToPublicDTO).collect(Collectors.toList()));
    }

    // (Профіль)
    @Operation(summary = "Отримати користувача за ID", description = "Повертає публічну інформацію про користувача за його ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Користувач успішно знайдено"),
            @ApiResponse(responseCode = "404", description = "Користувач не знайдено")
    })
    @GetMapping("/{id}")
    public ResponseEntity<UserPublicDTO> getUserById(@PathVariable UUID id) {
        // Очищено: якщо юзера немає, GlobalHandler поверне 404
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @Operation(summary = "Отримати поточного користувача", description = "Повертає інформацію про поточного авторизованого користувача")
    @ApiResponse(responseCode = "200", description = "Інформація про користувача успішно отримано")
    @GetMapping("/me")
    public ResponseEntity<UserPublicDTO> getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("User not found"));

        UserPublicDTO dto = mapToPublicDTO(currentUser);
        dto.setFollowing(false);
        return ResponseEntity.ok(dto);
    }

    @Operation(summary = "Оновити профіль користувача", description = "Оновлює дані поточного авторизованого користувача")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Профіль успішно оновлено"),
            @ApiResponse(responseCode = "400", description = "Невірні дані для оновлення")
    })
    @PatchMapping("/update")
    public ResponseEntity<String> updateProfile(@RequestBody UpdateUserRequest request) {
        userService.updateProfile(request);
        return ResponseEntity.ok("Profile updated successfully");
    }

    private UserPublicDTO mapToPublicDTO(User user) {
        UserPublicDTO dto = new UserPublicDTO();
        dto.setId(user.getId());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setEmail(user.getEmail());
        dto.setAvatarUrl(user.getAvatarUrl());
        dto.setBackgroundImg(user.getBackgroundImgUrl());
        dto.setNickName(user.getNickName());
        dto.setAboutMe(user.getAboutMe());
        dto.setFollowersCount(user.getFollowers() != null ? user.getFollowers().size() : 0);
        dto.setFollowingCount(user.getFollowing() != null ? user.getFollowing().size() : 0);
        dto.setPostsCount(user.getPosts() != null ? user.getPosts().size() : 0);
        dto.setFollowing(userService.isFollowing(user));
        return dto;
    }
}