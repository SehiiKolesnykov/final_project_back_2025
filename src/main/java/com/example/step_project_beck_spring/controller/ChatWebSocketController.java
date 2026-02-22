package com.example.step_project_beck_spring.controller;

import com.example.step_project_beck_spring.dto.chat.ChatMessageRequest;
import com.example.step_project_beck_spring.dto.chat.ChatMessageResponse;
import com.example.step_project_beck_spring.entities.User;
import com.example.step_project_beck_spring.service.chat.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;

import java.util.UUID;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat/send")
    public void handleChatMessage(@Payload ChatMessageRequest request,
                                  StompHeaderAccessor headerAccessor) {
        try {
            log.info("handleChatMessage: request received for threadId={}, recipient={}",
                    request.getThreadId(), request.getRecipientUserId());

            User user = extractUser(headerAccessor);
            if (user == null) {
                throw new IllegalStateException("User not authenticated");
            }

            request.setSenderUserId(user.getId());

            ChatMessageResponse response = chatService.sendMessage(request);

            String topic = "/topic/chat/" + response.getThreadId();
            messagingTemplate.convertAndSend(topic, response);

            log.info("Message sent to topic: {}, messageId={}", topic, response.getId());
        } catch (Exception e) {
            log.error("Error in handleChatMessage: {}", e.getMessage(), e);
            throw e;
        }
    }

    // Утилітний метод для витягування User з header/security context
    private User extractUser(StompHeaderAccessor headerAccessor) {
        Authentication auth = null;

        if (headerAccessor != null && headerAccessor.getUser() instanceof Authentication principalAuth) {
            auth = principalAuth;
        }

        if (auth == null) {
            auth = SecurityContextHolder.getContext().getAuthentication();
        }

        if (auth != null && auth.getPrincipal() instanceof User user) {
            return user;
        }

        return null;
    }
}