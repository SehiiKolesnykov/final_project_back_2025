package com.example.step_project_beck_spring.controller;

import com.example.step_project_beck_spring.dto.chat.ChatMessageResponse;
import com.example.step_project_beck_spring.dto.chat.ChatThreadResponse;
import com.example.step_project_beck_spring.entities.User;
import com.example.step_project_beck_spring.repository.UserRepository;
import com.example.step_project_beck_spring.service.CurrentUserService;
import com.example.step_project_beck_spring.service.chat.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Tag(name = "Chat", description = "API для роботи з чатом та повідомленнями")
public class ChatController {

    private final ChatService chatService;
    private final CurrentUserService currentUserService;
    private final UserRepository userRepository;

    @Operation(summary = "Отримати або створити тред з користувачем", description = "Створює новий тред або повертає існуючий тред між поточним користувачем та іншим користувачем")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Тред успішно отримано або створено"),
            @ApiResponse(responseCode = "404", description = "Користувач не знайдено")
    })
    @GetMapping("/thread/{otherUserId}")
    public ResponseEntity<ChatThreadResponse> getOrCreateThread(@PathVariable UUID otherUserId) {
        User currentUser = currentUserService.getCurrentUser();
        User otherUser = userRepository.findById(otherUserId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + otherUserId));
        
        ChatThreadResponse thread = chatService.getOrCreateThread(currentUser, otherUser);
        return ResponseEntity.ok(thread);
    }

    @Operation(summary = "Отримати всі треди користувача", description = "Повертає список всіх чат-тредів поточного користувача, відсортованих за часом оновлення")
    @ApiResponse(responseCode = "200", description = "Список тредів успішно отримано")
    @GetMapping("/threads")
    public ResponseEntity<List<ChatThreadResponse>> getThreads() {
        User currentUser = currentUserService.getCurrentUser();
        List<ChatThreadResponse> threads = chatService.getThreadsForUser(currentUser);
        return ResponseEntity.ok(threads);
    }

    @Operation(summary = "Отримати повідомлення треду", description = "Повертає останні 20 повідомлень з треду та відмічає їх як прочитані")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Повідомлення успішно отримано"),
            @ApiResponse(responseCode = "404", description = "Тред не знайдено")
    })
    @GetMapping("/thread/{threadId}/messages")
    public ResponseEntity<List<ChatMessageResponse>> getMessages(@PathVariable Long threadId) {
        User currentUser = currentUserService.getCurrentUser();
        List<ChatMessageResponse> messages = chatService.getMessagesForThread(threadId, currentUser);
        return ResponseEntity.ok(messages);
    }


    @Operation(summary = "Відмітити тред як прочитаний", description = "Оновлює статус прочитання повідомлень у треді для поточного користувача")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Тред успішно відмічено як прочитаний"),
            @ApiResponse(responseCode = "404", description = "Тред не знайдено")
    })
    @PostMapping("/thread/{threadId}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long threadId) {
        User currentUser = currentUserService.getCurrentUser();
        chatService.markThreadAsRead(threadId, currentUser);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Видалити тред", description = "Видаляє весь чат-тред з усіма повідомленнями. Доступно тільки для учасників треду")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Тред успішно видалено"),
            @ApiResponse(responseCode = "404", description = "Тред не знайдено"),
            @ApiResponse(responseCode = "403", description = "Користувач не є учасником треду")
    })
    @DeleteMapping("/thread/{threadId}")
    public ResponseEntity<Void> deleteThread(@PathVariable Long threadId) {
        User currentUser = currentUserService.getCurrentUser();
        chatService.deleteThread(threadId, currentUser);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Видалити повідомлення", description = "Видаляє окреме повідомлення з чату. Доступно тільки для автора повідомлення")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Повідомлення успішно видалено"),
            @ApiResponse(responseCode = "404", description = "Повідомлення не знайдено"),
            @ApiResponse(responseCode = "403", description = "Користувач не є автором повідомлення")
    })
    @DeleteMapping("/message/{messageId}")
    public ResponseEntity<Void> deleteMessage(@PathVariable Long messageId) {
        User currentUser = currentUserService.getCurrentUser();
        chatService.deleteMessage(messageId, currentUser);
        return ResponseEntity.ok().build();
    }
}

