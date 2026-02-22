package com.example.step_project_beck_spring.dto.chat;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public class ChatMessageRequest {

    private UUID threadId;

    private UUID recipientUserId;

    @NotBlank(message = "Message content is required")
    private String content;

    private UUID senderUserId;

    public ChatMessageRequest() {
    }

    public ChatMessageRequest(UUID threadId, UUID recipientUserId, String content) {
        this.threadId = threadId;
        this.recipientUserId = recipientUserId;
        this.content = content;
    }

    public UUID getThreadId() {
        return threadId;
    }

    public void setThreadId(UUID threadId) {
        this.threadId = threadId;
    }

    public UUID getRecipientUserId() {
        return recipientUserId;
    }

    public void setRecipientUserId(UUID recipientUserId) {
        this.recipientUserId = recipientUserId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public UUID getSenderUserId() {
        return senderUserId;
    }

    public void setSenderUserId(UUID senderUserId) {
        this.senderUserId = senderUserId;
    }
}

