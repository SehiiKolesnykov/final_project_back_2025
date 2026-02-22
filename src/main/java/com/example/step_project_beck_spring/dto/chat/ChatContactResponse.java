package com.example.step_project_beck_spring.dto.chat;

import java.util.UUID;

public class ChatContactResponse {

    private UUID id;
    private String nickName;
    private String email;
    private String avatarUrl;  // ← Додано

    public ChatContactResponse() {
    }

    public ChatContactResponse(UUID id, String nickName, String avatarUrl) {
        this.id = id;
        this.nickName = nickName;
        this.avatarUrl = avatarUrl;
        this.email = null;
    }

    public ChatContactResponse(UUID id, String nickName, String email, String avatarUrl) {
        this.id = id;
        this.nickName = nickName;
        this.email = email;
        this.avatarUrl = avatarUrl;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
}