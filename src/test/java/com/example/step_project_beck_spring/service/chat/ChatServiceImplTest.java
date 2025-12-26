package com.example.step_project_beck_spring.service.chat;

import com.example.step_project_beck_spring.dto.chat.ChatMessageRequest;
import com.example.step_project_beck_spring.dto.chat.ChatMessageResponse;
import com.example.step_project_beck_spring.dto.chat.ChatThreadResponse;
import com.example.step_project_beck_spring.entities.ChatMessage;
import com.example.step_project_beck_spring.entities.ChatThread;
import com.example.step_project_beck_spring.entities.User;
import com.example.step_project_beck_spring.repository.ChatMessageRepository;
import com.example.step_project_beck_spring.repository.ChatThreadRepository;
import com.example.step_project_beck_spring.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatServiceImplTest {

    @Mock
    private ChatThreadRepository threadRepository;

    @Mock
    private ChatMessageRepository messageRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ChatServiceImpl chatService;

    private User user1;
    private User user2;
    private ChatThread thread;
    private ChatMessage message;

    @BeforeEach
    void setUp() {
        user1 = new User();
        user1.setId(UUID.randomUUID());
        user1.setEmail("user1@test.com");
        user1.setFirstName("User");
        user1.setLastName("One");

        user2 = new User();
        user2.setId(UUID.randomUUID());
        user2.setEmail("user2@test.com");
        user2.setFirstName("User");
        user2.setLastName("Two");

        thread = new ChatThread();
        thread.setId(1L);
        thread.setParticipants(Set.of(user1, user2));
        thread.setCreatedAt(LocalDateTime.now());

        message = new ChatMessage();
        message.setId(1L);
        message.setThread(thread);
        message.setSender(user1);
        message.setContent("Test message");
        message.setMessageType(ChatMessage.MessageType.TEXT);
        message.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void getOrCreateThread_WhenThreadExists_ShouldReturnExistingThread() {
        // Given
        when(threadRepository.findThreadBetween(user1, user2))
                .thenReturn(Optional.of(thread));

        // When
        ChatThreadResponse response = chatService.getOrCreateThread(user1, user2);

        // Then
        assertNotNull(response);
        assertEquals(thread.getId(), response.getId());
        verify(threadRepository, times(1)).findThreadBetween(user1, user2);
        verify(threadRepository, never()).save(any());
    }

    @Test
    void getOrCreateThread_WhenThreadNotExists_ShouldCreateNewThread() {
        // Given
        when(threadRepository.findThreadBetween(user1, user2))
                .thenReturn(Optional.empty());
        when(threadRepository.save(any(ChatThread.class)))
                .thenReturn(thread);

        // When
        ChatThreadResponse response = chatService.getOrCreateThread(user1, user2);

        // Then
        assertNotNull(response);
        assertEquals(thread.getId(), response.getId());
        verify(threadRepository, times(1)).findThreadBetween(user1, user2);
        verify(threadRepository, times(1)).save(any(ChatThread.class));
    }

    @Test
    void getThreadsForUser_ShouldReturnListOfThreads() {
        // Given
        List<ChatThread> threads = List.of(thread);
        when(threadRepository.findAllByParticipant(user1))
                .thenReturn(threads);

        // When
        List<ChatThreadResponse> responses = chatService.getThreadsForUser(user1);

        // Then
        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(thread.getId(), responses.get(0).getId());
        verify(threadRepository, times(1)).findAllByParticipant(user1);
    }

    @Test
    void getMessagesForThread_WhenThreadExists_ShouldReturnMessages() {
        // Given
        when(threadRepository.findById(1L))
                .thenReturn(Optional.of(thread));
        when(messageRepository.findByThreadOrderByCreatedAtAsc(thread))
                .thenReturn(List.of(message));

        // When
        List<ChatMessageResponse> responses = chatService.getMessagesForThread(1L, user1);

        // Then
        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(message.getId(), responses.get(0).getId());
        verify(threadRepository, times(1)).findById(1L);
        verify(messageRepository, times(1)).findByThreadOrderByCreatedAtAsc(thread);
    }

    @Test
    void getMessagesForThread_WhenThreadNotFound_ShouldThrowException() {
        // Given
        when(threadRepository.findById(1L))
                .thenReturn(Optional.empty());

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> {
            chatService.getMessagesForThread(1L, user1);
        });
    }

    @Test
    void getMessagesForThread_WhenUserNotParticipant_ShouldThrowException() {
        // Given
        User nonParticipant = new User();
        nonParticipant.setId(UUID.randomUUID());
        when(threadRepository.findById(1L))
                .thenReturn(Optional.of(thread));

        // When & Then
        assertThrows(IllegalStateException.class, () -> {
            chatService.getMessagesForThread(1L, nonParticipant);
        });
    }

    @Test
    void sendMessage_WithThreadId_ShouldSendMessage() {
        // Given
        ChatMessageRequest request = new ChatMessageRequest();
        request.setThreadId(1L);
        request.setSenderUserId(user1.getId());
        request.setContent("Test message");

        when(userRepository.findById(user1.getId()))
                .thenReturn(Optional.of(user1));
        when(threadRepository.findById(1L))
                .thenReturn(Optional.of(thread));
        when(messageRepository.save(any(ChatMessage.class)))
                .thenReturn(message);
        when(threadRepository.save(any(ChatThread.class)))
                .thenReturn(thread);

        // When
        ChatMessageResponse response = chatService.sendMessage(request);

        // Then
        assertNotNull(response);
        assertEquals(message.getId(), response.getId());
        verify(userRepository, times(1)).findById(user1.getId());
        verify(threadRepository, times(1)).findById(1L);
        verify(messageRepository, times(1)).save(any(ChatMessage.class));
        verify(threadRepository, times(1)).save(any(ChatThread.class));
    }

    @Test
    void sendMessage_WithRecipientUserId_ShouldCreateOrFindThread() {
        // Given
        ChatMessageRequest request = new ChatMessageRequest();
        request.setRecipientUserId(user2.getId());
        request.setSenderUserId(user1.getId());
        request.setContent("Test message");

        when(userRepository.findById(user1.getId()))
                .thenReturn(Optional.of(user1));
        when(userRepository.findById(user2.getId()))
                .thenReturn(Optional.of(user2));
        when(threadRepository.findThreadBetween(user1, user2))
                .thenReturn(Optional.of(thread));
        when(messageRepository.save(any(ChatMessage.class)))
                .thenReturn(message);
        when(threadRepository.save(any(ChatThread.class)))
                .thenReturn(thread);

        // When
        ChatMessageResponse response = chatService.sendMessage(request);

        // Then
        assertNotNull(response);
        verify(threadRepository, times(1)).findThreadBetween(user1, user2);
        verify(messageRepository, times(1)).save(any(ChatMessage.class));
    }

    @Test
    void sendMessage_WhenSenderNotFound_ShouldThrowException() {
        // Given
        ChatMessageRequest request = new ChatMessageRequest();
        request.setSenderUserId(UUID.randomUUID());
        request.setContent("Test message");

        when(userRepository.findById(any(UUID.class)))
                .thenReturn(Optional.empty());

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> {
            chatService.sendMessage(request);
        });
    }

    @Test
    void sendMessage_WhenThreadNotFound_ShouldThrowException() {
        // Given
        ChatMessageRequest request = new ChatMessageRequest();
        request.setThreadId(999L);
        request.setSenderUserId(user1.getId());
        request.setContent("Test message");

        when(userRepository.findById(user1.getId()))
                .thenReturn(Optional.of(user1));
        when(threadRepository.findById(999L))
                .thenReturn(Optional.empty());

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> {
            chatService.sendMessage(request);
        });
    }

    @Test
    void sendMessage_WhenNoThreadIdOrRecipient_ShouldThrowException() {
        // Given
        ChatMessageRequest request = new ChatMessageRequest();
        request.setSenderUserId(user1.getId());
        request.setContent("Test message");
        // No threadId and no recipientUserId

        when(userRepository.findById(user1.getId()))
                .thenReturn(Optional.of(user1));

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            chatService.sendMessage(request);
        });
    }

    @Test
    void sendMessage_WhenUserNotParticipant_ShouldThrowException() {
        // Given
        User nonParticipant = new User();
        nonParticipant.setId(UUID.randomUUID());
        ChatMessageRequest request = new ChatMessageRequest();
        request.setThreadId(1L);
        request.setSenderUserId(nonParticipant.getId());
        request.setContent("Test message");

        when(userRepository.findById(nonParticipant.getId()))
                .thenReturn(Optional.of(nonParticipant));
        when(threadRepository.findById(1L))
                .thenReturn(Optional.of(thread));

        // When & Then
        assertThrows(IllegalStateException.class, () -> {
            chatService.sendMessage(request);
        });
    }
}

