package com.example.step_project_beck_spring.controller;

import com.example.step_project_beck_spring.dto.chat.ChatMessageRequest;
import com.example.step_project_beck_spring.dto.chat.ChatMessageResponse;
import com.example.step_project_beck_spring.entities.User;
import com.example.step_project_beck_spring.service.chat.ChatService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;

import java.util.UUID;

@Controller
public class ChatWebSocketController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatWebSocketController(ChatService chatService, 
                                   SimpMessagingTemplate messagingTemplate) {
        this.chatService = chatService;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/chat/send")
    public void handleChatMessage(@Payload ChatMessageRequest request, 
                                  StompHeaderAccessor headerAccessor) {
        try {
            System.out.println("========================================");
            System.out.println("=== ChatWebSocketController.handleChatMessage ===");
            System.out.println("========================================");
            System.out.println("Received request: " + request);
            System.out.println("Request content: " + request.getContent());
            System.out.println("Request threadId: " + request.getThreadId());
            System.out.println("Request recipientUserId: " + request.getRecipientUserId());
            
            // Отримуємо User з SecurityContext або з StompHeaderAccessor
            // ВАЖЛИВО: WebSocket використовує асинхронні потоки, тому SecurityContext може бути порожнім
            // Спочатку намагаємося отримати з StompHeaderAccessor (який встановлюється в preSend)
            User user = null;
            Authentication auth = null;
            
            System.out.println("Current thread: " + Thread.currentThread().getName());
            
            // Спочатку намагаємося отримати з StompHeaderAccessor (найнадійніший спосіб)
            if (headerAccessor != null) {
                java.security.Principal principal = headerAccessor.getUser();
                System.out.println("Principal from headerAccessor: " + (principal != null ? principal.getClass().getName() : "null"));
                
                if (principal instanceof Authentication principalAuth) {
                    auth = principalAuth;
                    System.out.println("Found Authentication in StompHeaderAccessor");
                } else if (principal instanceof User userPrincipal) {
                    auth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                            userPrincipal, null, userPrincipal.getAuthorities());
                    System.out.println("Created Authentication from User in StompHeaderAccessor");
                }
            }
            
            // Якщо не знайшли в StompHeaderAccessor, намагаємося з SecurityContext
            if (auth == null) {
                System.out.println("Trying SecurityContext...");
                auth = SecurityContextHolder.getContext().getAuthentication();
                System.out.println("Authentication from SecurityContext: " + (auth != null ? auth.getClass().getName() : "null"));
            }
            
            // Перевіряємо, чи є валідний User
            if (auth != null && auth.getPrincipal() instanceof User) {
                user = (User) auth.getPrincipal();
                System.out.println("User found: " + user.getEmail());
            } else {
                System.err.println("User is null! Authentication failed.");
                System.err.println("Auth is null: " + (auth == null));
                if (auth != null) {
                    System.err.println("Principal is null: " + (auth.getPrincipal() == null));
                    if (auth.getPrincipal() != null) {
                        System.err.println("Principal type: " + auth.getPrincipal().getClass().getName());
                    }
                }
                throw new org.springframework.security.access.AccessDeniedException("Not authenticated");
            }
            System.out.println("Authenticated user: " + user.getEmail());
            System.out.println("User ID: " + user.getId());
            
            // Встановлюємо senderUserId з поточного автентифікованого користувача
            UUID senderId = user.getId();
            System.out.println("Setting senderUserId: " + senderId);
            request.setSenderUserId(senderId);
            
            System.out.println("Calling chatService.sendMessage...");
            ChatMessageResponse response = chatService.sendMessage(request);
            System.out.println("Message saved, response: " + response);
            System.out.println("Response ID: " + response.getId());
            System.out.println("Response threadId: " + response.getThreadId());
            System.out.println("Response content: " + response.getContent());
            
            // Відправляємо повідомлення на тред, щоб обидва учасники отримали його
            String topic = "/topic/chat/" + response.getThreadId();
            System.out.println("Sending message to topic: " + topic);
            messagingTemplate.convertAndSend(topic, response);
            
            System.out.println("Повідомлення відправлено на тред: " + topic);
            System.out.println("Thread ID: " + response.getThreadId());
            System.out.println("Message ID: " + response.getId());
            System.out.println("========================================");
        } catch (Exception e) {
            System.err.println("ERROR in handleChatMessage");
            System.err.println("Error message: " + e.getMessage());
            System.err.println("Error class: " + e.getClass().getName());
            e.printStackTrace();
            throw e;
        }
    }
}

