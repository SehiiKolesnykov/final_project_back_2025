package com.example.step_project_beck_spring.controller;

import com.example.step_project_beck_spring.dto.NotificationPageDto;
import com.example.step_project_beck_spring.entities.User;
import com.example.step_project_beck_spring.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;

    //Отримати сторінку сповіщень поточного користувача.
    @GetMapping
    public ResponseEntity<NotificationPageDto> getMyNotifications(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        User currentUser = (User) authentication.getPrincipal();
        NotificationPageDto dto = notificationService.getNotifications(currentUser.getId(), page, size);
        return ResponseEntity.ok(dto);
    }

    //Позначити конкретну нотифікацію як прочитану.
    @PostMapping("/{notificationId}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable UUID notificationId,
                                           Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        notificationService.markAsRead(notificationId, currentUser.getId());
        return ResponseEntity.ok().build();
    }

    //Позначити всі нотифікації як прочитані.
    @PostMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        notificationService.markAllAsRead(currentUser.getId());
        return ResponseEntity.ok().build();
    }

    //Отримати кількість непрочитаних нотифікацій (для бейджика).
    @GetMapping("/unread-count")
    public ResponseEntity<Long> getUnreadCount(Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        long count = notificationService.countUnread(currentUser.getId());
        return ResponseEntity.ok(count);
    }
}


