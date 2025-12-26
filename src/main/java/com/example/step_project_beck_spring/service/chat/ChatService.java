package com.example.step_project_beck_spring.service.chat;

import com.example.step_project_beck_spring.dto.chat.ChatMessageRequest;
import com.example.step_project_beck_spring.dto.chat.ChatMessageResponse;
import com.example.step_project_beck_spring.dto.chat.ChatThreadResponse;
import com.example.step_project_beck_spring.entities.User;

import java.util.List;

public interface ChatService {

    ChatThreadResponse getOrCreateThread(User currentUser, User otherUser);

    List<ChatThreadResponse> getThreadsForUser(User user);

    List<ChatMessageResponse> getMessagesForThread(Long threadId, User user);

    ChatMessageResponse sendMessage(ChatMessageRequest request);
    
    void markThreadAsRead(Long threadId, User user);
}

