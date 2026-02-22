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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat/send")
    public void handleChatMessage(
            @Payload ChatMessageRequest request,
            @AuthenticationPrincipal User currentUser  // ← тепер беремо з SecurityContext!
    ) {
        try {
            log.info("handleChatMessage: request received for threadId={}, recipient={}, from user={}",
                    request.getThreadId(), request.getRecipientUserId(), currentUser.getEmail());

            // Встановлюємо sender з автентифікованого користувача
            request.setSenderUserId(currentUser.getId());

            ChatMessageResponse response = chatService.sendMessage(request);

            String topic = "/topic/chat/" + response.getThreadId();
            messagingTemplate.convertAndSend(topic, response);

            log.info("Message sent to topic: {}, messageId={}", topic, response.getId());
        } catch (Exception e) {
            log.error("Error in handleChatMessage: {}", e.getMessage(), e);
            throw e;
        }
    }
}