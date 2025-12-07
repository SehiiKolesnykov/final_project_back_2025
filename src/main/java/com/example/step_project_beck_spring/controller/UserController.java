package com.example.step_project_beck_spring.controller;

import com.example.step_project_beck_spring.dto.UserPublicDTO;
import com.example.step_project_beck_spring.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;
import java.util.NoSuchElementException;

/** REST-контролер для управління користувачами */
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    /** GET /api/user/{id} */
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
    /** GET /api/user/by-email?email=test@example.com */
    @GetMapping("/by-email")
    public ResponseEntity<UserPublicDTO> getUserByEmail(@RequestParam String email) {
        try {
            UserPublicDTO user = userService.getUserByEmail(email);
            return ResponseEntity.ok(user); // 200 OK
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build(); // 404 Not Found
        }
    }
}