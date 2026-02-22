package com.example.step_project_beck_spring.dto.chat;


import com.example.step_project_beck_spring.entities.ChatMessage;

import java.time.LocalDateTime;
import java.util.UUID;

public class ChatMessageResponse {

    private Long id;
    private Long threadId;
    private UUID senderId;
    private String senderNickName;
    private String content;
    private LocalDateTime createdAt;
    private String messageType;


    public ChatMessageResponse() {
    }

    public ChatMessageResponse(Long id, Long threadId, UUID senderId, String senderNickName,
                               String content, LocalDateTime createdAt, String messageType) {
        this.id = id;
        this.threadId = threadId;
        this.senderId = senderId;
        this.senderNickName = senderNickName;
        this.content = content;
        this.createdAt = createdAt;
        this.messageType = messageType;
    }

    public static ChatMessageResponse from(ChatMessage message) {
        ChatMessageResponse response = new ChatMessageResponse(
                message.getId(),
                message.getThread().getId(),
                message.getSender().getId(),
                message.getSender().getNickName(),
                message.getContent(),
                message.getCreatedAt(),
                message.getMessageType() != null ? message.getMessageType().name() : "TEXT"
        );



        return response;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getThreadId() {
        return threadId;
    }

    public void setThreadId(Long threadId) {
        this.threadId = threadId;
    }

    public UUID getSenderId() {
        return senderId;
    }

    public void setSenderId(UUID senderId) {
        this.senderId = senderId;
    }

    public String getSenderNickName() {
        return senderNickName;
    }

    public void setSenderNickName(String senderNickName) {
        this.senderNickName = senderNickName;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

}