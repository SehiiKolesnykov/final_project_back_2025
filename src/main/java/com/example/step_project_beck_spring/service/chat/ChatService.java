package com.example.step_project_beck_spring.service.chat;

import com.example.step_project_beck_spring.dto.chat.ChatMessageRequest;
import com.example.step_project_beck_spring.dto.chat.ChatMessageResponse;
import com.example.step_project_beck_spring.dto.chat.ChatThreadResponse;
import com.example.step_project_beck_spring.entities.User;

import java.util.List;
import java.util.UUID;

public interface ChatService {

    ChatThreadResponse getOrCreateThread(User currentUser, User otherUser);

    List<ChatThreadResponse> getThreadsForUser(User user);

    List<ChatMessageResponse> getMessagesForThread(UUID threadId, User user);

    ChatMessageResponse sendMessage(ChatMessageRequest request);
    
    void deleteThread(UUID threadId, User user);
    
    void deleteMessage(UUID messageId, User user);
    
    void markThreadAsRead(UUID threadId, User user);
}

