package com.example.step_project_beck_spring.controller;

import com.example.step_project_beck_spring.dto.chat.ChatMessageRequest;
import com.example.step_project_beck_spring.dto.chat.ChatMessageResponse;
import com.example.step_project_beck_spring.entities.User;
import com.example.step_project_beck_spring.service.chat.ChatService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatWebSocketControllerTest {

    @Mock
    private ChatService chatService;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private ChatWebSocketController chatWebSocketController;

    private ChatMessageRequest request;
    private ChatMessageResponse response;
    private User user;

    @BeforeEach
    void setUp() {
        request = new ChatMessageRequest();
        request.setContent("Test message");

        response = new ChatMessageResponse();
        response.setId(1L);
        response.setThreadId(1L);
        response.setContent("Test message");

        user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("test@example.com");
        user.setFirstName("Test");
        user.setLastName("User");
        
        // Встановлюємо SecurityContext для тестів
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void handleChatMessage_ShouldProcessAndSendMessage() {
        // Given
        when(chatService.sendMessage(request)).thenReturn(response);

        // When
        //chatWebSocketController.handleChatMessage(request);

        // Then
        verify(chatService, times(1)).sendMessage(request);
        verify(messagingTemplate, times(1))
                .convertAndSend(eq("/topic/chat/1"), eq(response));
    }

    @Test
    void handleChatMessage_WithDifferentThreadId_ShouldSendToCorrectTopic() {
        // Given
        response.setThreadId(999L);
        when(chatService.sendMessage(request)).thenReturn(response);

        // When
        // chatWebSocketController.handleChatMessage(request);

        // Then
        verify(chatService, times(1)).sendMessage(request);
        verify(messagingTemplate, times(1))
                .convertAndSend(eq("/topic/chat/999"), eq(response));
    }
}

