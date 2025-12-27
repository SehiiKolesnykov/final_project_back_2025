package com.example.step_project_beck_spring.controller;

import com.example.step_project_beck_spring.dto.chat.ChatMessageResponse;
import com.example.step_project_beck_spring.dto.chat.ChatThreadResponse;
import com.example.step_project_beck_spring.entities.User;
import com.example.step_project_beck_spring.repository.UserRepository;
import com.example.step_project_beck_spring.service.CurrentUserService;
import com.example.step_project_beck_spring.service.chat.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final CurrentUserService currentUserService;
    private final UserRepository userRepository;

    @GetMapping("/thread/{otherUserId}")
    public ResponseEntity<ChatThreadResponse> getOrCreateThread(@PathVariable UUID otherUserId) {
        User currentUser = currentUserService.getCurrentUser();
        User otherUser = userRepository.findById(otherUserId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + otherUserId));
        
        ChatThreadResponse thread = chatService.getOrCreateThread(currentUser, otherUser);
        return ResponseEntity.ok(thread);
    }

    @GetMapping("/threads")
    public ResponseEntity<List<ChatThreadResponse>> getThreads() {
        User currentUser = currentUserService.getCurrentUser();
        List<ChatThreadResponse> threads = chatService.getThreadsForUser(currentUser);
        return ResponseEntity.ok(threads);
    }

    @GetMapping("/thread/{threadId}/messages")
    public ResponseEntity<List<ChatMessageResponse>> getMessages(@PathVariable Long threadId) {
        User currentUser = currentUserService.getCurrentUser();
        List<ChatMessageResponse> messages = chatService.getMessagesForThread(threadId, currentUser);
        return ResponseEntity.ok(messages);
    }


    @PostMapping("/thread/{threadId}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long threadId) {
        User currentUser = currentUserService.getCurrentUser();
        chatService.markThreadAsRead(threadId, currentUser);
        return ResponseEntity.ok().build();
    }
}

