package com.example.step_project_beck_spring.dto.chat;

import com.example.step_project_beck_spring.entities.ChatThread;
import com.example.step_project_beck_spring.entities.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class ChatThreadResponse {

    private UUID id;
    private List<ChatContactResponse> participants;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long unreadCount;
    private ChatContactResponse otherParticipant;

    public ChatThreadResponse() {
    }

    public ChatThreadResponse(UUID id, List<ChatContactResponse> participants,
                              LocalDateTime createdAt, LocalDateTime updatedAt, Long unreadCount, ChatContactResponse otherParticipant) {
        this.id = id;
        this.participants = participants;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.unreadCount = unreadCount;
        this.otherParticipant = otherParticipant;
    }

    public static ChatThreadResponse from(ChatThread thread, User currentUser, Long unreadCount) {
        List<ChatContactResponse> participants = thread.getParticipants().stream()
                .map(user -> new ChatContactResponse(user.getId(), user.getNickName(), user.getEmail()))
                .collect(Collectors.toList());


        ChatContactResponse otherParticipant = thread.getParticipants().stream()
                .filter(user -> !user.getId().equals(currentUser.getId()))
                .findFirst()
                .map(user -> new ChatContactResponse(user.getId(), user.getNickName(), user.getEmail()))
                .orElse(null);

        return new ChatThreadResponse(
                thread.getId(),
                participants,
                thread.getCreatedAt(),
                thread.getUpdatedAt(),
                unreadCount != null ? unreadCount : 0L,
                otherParticipant
        );
    }


    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public List<ChatContactResponse> getParticipants() {
        return participants;
    }

    public void setParticipants(List<ChatContactResponse> participants) {
        this.participants = participants;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Long getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(Long unreadCount) {
        this.unreadCount = unreadCount;
    }

    public ChatContactResponse getOtherParticipant() {
        return otherParticipant;
    }

    public void setOtherParticipant(ChatContactResponse otherParticipant) {
        this.otherParticipant = otherParticipant;
    }
}