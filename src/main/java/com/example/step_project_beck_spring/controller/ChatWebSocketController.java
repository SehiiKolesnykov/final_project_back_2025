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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatWebSocketController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat/send")
    public void handleChatMessage(@Payload ChatMessageRequest request,
                                  Principal principal) {

        if (principal == null) {
            throw new AccessDeniedException("User not authenticated");
        }

        User user = (User) ((UsernamePasswordAuthenticationToken) principal).getPrincipal();
        request.setSenderUserId(user.getId());
        log.info("WebSocket message from: {}", user.getEmail());
        ChatMessageResponse response = chatService.sendMessage(request);
        String topic = "/topic/chat/" + response.getThreadId();
        messagingTemplate.convertAndSend(topic, response);
    }
}