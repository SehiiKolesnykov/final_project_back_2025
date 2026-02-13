package com.example.step_project_beck_spring.controller;

import com.example.step_project_beck_spring.dto.UserPrivateDTO;
import com.example.step_project_beck_spring.dto.UserPublicDTO;
import com.example.step_project_beck_spring.entities.User;
import com.example.step_project_beck_spring.repository.UserRepository;
import com.example.step_project_beck_spring.service.CurrentUserService;
import com.example.step_project_beck_spring.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

/** REST-контролер для управління користувачами */
@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Tag(name = "Users", description = "API для управління користувачами та профілями")
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;

    /** GET /api/user - отримати всіх користувачів (виключає поточного) */
    @Operation(summary = "Отримати всіх користувачів", description = "Повертає список всіх користувачів, виключаючи поточного авторизованого користувача")
    @ApiResponse(responseCode = "200", description = "Список користувачів успішно отримано")
    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<List<UserPublicDTO>> getAllUsers() {
        UUID currentUserId = currentUserService.getCurrentUserId();
        List<UserPublicDTO> users = userRepository.findAll().stream()
                .filter(user -> !user.getId().equals(currentUserId)) // Виключаємо поточного користувача
                .map(user -> {
                    UserPublicDTO dto = new UserPublicDTO();
                    dto.setId(user.getId());
                    dto.setFirstName(user.getFirstName());
                    dto.setLastName(user.getLastName());
                    dto.setAvatarUrl(user.getAvatarUrl());
                    dto.setBackgroundImg(user.getBackgroundImgUrl());
                    // Безпечно отримуємо розміри колекцій в межах транзакції
                    try {
                        dto.setFollowersCount(user.getFollowers() != null ? user.getFollowers().size() : 0);
                        dto.setFollowingCount(user.getFollowing() != null ? user.getFollowing().size() : 0);
                        dto.setPostsCount(user.getPosts() != null ? user.getPosts().size() : 0);
                    } catch (Exception e) {
                        // Якщо колекції не завантажені, встановлюємо 0
                        dto.setFollowersCount(0);
                        dto.setFollowingCount(0);
                        dto.setPostsCount(0);
                    }
                    dto.setFollowing(false); // TODO: implement following check
                    return dto;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }

    /** GET /api/user/search?q=query - пошук користувачів за ім'ям або email */
    @Operation(summary = "Пошук користувачів", description = "Шукає користувачів за ім'ям, прізвищем або email")
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

        UUID currentUserId = currentUserService.getCurrentUserId();
        List<User> foundUsers = userRepository.searchUsers(q.trim(), currentUserId);

        List<UserPublicDTO> users = foundUsers.stream()
                .map(user -> {
                    UserPublicDTO dto = new UserPublicDTO();
                    dto.setId(user.getId());
                    dto.setFirstName(user.getFirstName());
                    dto.setLastName(user.getLastName());
                    dto.setAvatarUrl(user.getAvatarUrl());
                    dto.setBackgroundImg(user.getBackgroundImgUrl());
                    // Безпечно отримуємо розміри колекцій в межах транзакції
                    try {
                        dto.setFollowersCount(user.getFollowers() != null ? user.getFollowers().size() : 0);
                        dto.setFollowingCount(user.getFollowing() != null ? user.getFollowing().size() : 0);
                        dto.setPostsCount(user.getPosts() != null ? user.getPosts().size() : 0);
                    } catch (Exception e) {
                        // Якщо колекції не завантажені, встановлюємо 0
                        dto.setFollowersCount(0);
                        dto.setFollowingCount(0);
                        dto.setPostsCount(0);
                    }
                    dto.setFollowing(false);
                    return dto;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(users);
    }

    /** GET /api/user/{id} - отримати користувача за ID */
    @Operation(summary = "Отримати користувача за ID", description = "Повертає публічну інформацію про користувача за його ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Користувач успішно знайдено"),
            @ApiResponse(responseCode = "404", description = "Користувач не знайдено")
    })
    @GetMapping("/{id}")
    public ResponseEntity<UserPublicDTO> getUserById(@PathVariable UUID id) {
        try {
            UserPublicDTO user = userService.getUserById(id);
            return ResponseEntity.ok(user); // 200 OK
        } catch (NoSuchElementException e) {
            /** Перетворюємо помилку на HTTP-статус 404 Not Found */
            return ResponseEntity.notFound().build();
        }
    }

    /** GET /api/user/by-email?email=test@example.com - отримати користувача за email */
    @GetMapping("/by-email")
    public ResponseEntity<UserPublicDTO> getUserByEmail(@RequestParam String email) {
        try {
            UserPublicDTO user = userService.getUserByEmail(email);
            return ResponseEntity.ok(user); // 200 OK
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build(); // 404 Not Found
        }
    }

    /** GET /api/user/me - отримати поточного авторизованого користувача */
    @Operation(summary = "Отримати поточного користувача", description = "Повертає повну інформацію про поточного авторизованого користувача")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Інформація про користувача успішно отримано"),
            @ApiResponse(responseCode = "401", description = "Користувач не авторизований")
    })
    @GetMapping("/me")
    @Transactional(readOnly = true)
    public ResponseEntity<UserPrivateDTO> getCurrentUser() {
        try {
            // Отримуємо ID поточного користувача
            UUID currentUserId = currentUserService.getCurrentUserId();
            
            // Завантажуємо користувача з бази з колекціями в межах транзакції
            User currentUser = userRepository.findByIdWithCollections(currentUserId)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            UserPrivateDTO dto = new UserPrivateDTO();
            dto.setId(currentUser.getId());
            dto.setFirstName(currentUser.getFirstName());
            dto.setLastName(currentUser.getLastName());
            dto.setEmail(currentUser.getEmail());
            dto.setBirthDate(currentUser.getBirthDate());
            dto.setEmailVerified(currentUser.isEmailVerified());
            dto.setAvatarUrl(currentUser.getAvatarUrl());
            dto.setBackgroundImg(currentUser.getBackgroundImgUrl());
            // Колекції вже завантажені через JOIN FETCH
            dto.setFollowersCount(currentUser.getFollowers() != null ? currentUser.getFollowers().size() : 0);
            dto.setFollowingCount(currentUser.getFollowing() != null ? currentUser.getFollowing().size() : 0);
            dto.setPostsCount(currentUser.getPosts() != null ? currentUser.getPosts().size() : 0);
            dto.setFollowing(false);
            
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    /** PUT /api/user/me - оновити дані поточного користувача */
    @Operation(summary = "Оновити профіль користувача", description = "Оновлює дані поточного авторизованого користувача")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Профіль успішно оновлено"),
            @ApiResponse(responseCode = "400", description = "Невірні дані для оновлення"),
            @ApiResponse(responseCode = "401", description = "Користувач не авторизований")
    })
    @PutMapping("/me")
    public ResponseEntity<UserPrivateDTO> updateCurrentUser(
            @RequestBody Map<String, Object> userData) {
        try {
            User currentUser = currentUserService.getCurrentUser();
            
            // Оновлюємо поля, якщо вони надані
            if (userData.containsKey("firstName")) {
                currentUser.setFirstName((String) userData.get("firstName"));
            }
            if (userData.containsKey("lastName")) {
                currentUser.setLastName((String) userData.get("lastName"));
            }
            if (userData.containsKey("email")) {
                String email = (String) userData.get("email");
                if (email != null && !email.trim().isEmpty()) {
                    // Перевіряємо, чи email не зайнятий іншим користувачем
                    userRepository.findByEmail(email)
                            .ifPresent(existingUser -> {
                                if (!existingUser.getId().equals(currentUser.getId())) {
                                    throw new IllegalArgumentException("Email already in use");
                                }
                            });
                    currentUser.setEmail(email.trim());
                }
            }
            if (userData.containsKey("birthDate")) {
                Object birthDateObj = userData.get("birthDate");
                if (birthDateObj != null) {
                    if (birthDateObj instanceof String) {
                        currentUser.setBirthDate(LocalDate.parse((String) birthDateObj));
                    } else if (birthDateObj instanceof LocalDate) {
                        currentUser.setBirthDate((LocalDate) birthDateObj);
                    }
                }
            }
            if (userData.containsKey("avatarUrl")) {
                currentUser.setAvatarUrl((String) userData.get("avatarUrl"));
            }
            if (userData.containsKey("backgroundImgUrl")) {
                currentUser.setBackgroundImgUrl((String) userData.get("backgroundImgUrl"));
            }
            
            User updatedUser = userRepository.save(currentUser);
            
            UserPrivateDTO dto = new UserPrivateDTO();
            dto.setId(updatedUser.getId());
            dto.setFirstName(updatedUser.getFirstName());
            dto.setLastName(updatedUser.getLastName());
            dto.setEmail(updatedUser.getEmail());
            dto.setBirthDate(updatedUser.getBirthDate());
            dto.setEmailVerified(updatedUser.isEmailVerified());
            dto.setAvatarUrl(updatedUser.getAvatarUrl());
            dto.setBackgroundImg(updatedUser.getBackgroundImgUrl());
            dto.setFollowersCount(updatedUser.getFollowers() != null ? updatedUser.getFollowers().size() : 0);
            dto.setFollowingCount(updatedUser.getFollowing() != null ? updatedUser.getFollowing().size() : 0);
            dto.setPostsCount(updatedUser.getPosts() != null ? updatedUser.getPosts().size() : 0);
            dto.setFollowing(false);
            
            return ResponseEntity.ok(dto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    /** DELETE /api/user/{id} - видалити користувача */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}