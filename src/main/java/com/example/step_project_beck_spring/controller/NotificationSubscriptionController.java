package com.example.step_project_beck_spring.controller;

import com.example.step_project_beck_spring.entities.User;
import com.example.step_project_beck_spring.service.NotificationSubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications/subscriptions")
@RequiredArgsConstructor
public class NotificationSubscriptionController {
    private final NotificationSubscriptionService subscriptionService;

    //Увімкнути сповіщення про нового користувача.
    @PostMapping("/{targetUserId}")
    public ResponseEntity<Void> subscribe(@PathVariable UUID targetUserId,
                                          Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        subscriptionService.subscribe(currentUser.getId(), targetUserId);
        return ResponseEntity.ok().build();
    }

    //Вимкнути сповіщення.
    @DeleteMapping("/{targetUserId}")
    public ResponseEntity<Void> unsubscribe(@PathVariable UUID targetUserId,
                                            Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        subscriptionService.unsubscribe(currentUser.getId(), targetUserId);
        return ResponseEntity.noContent().build();
    }
}


