package com.example.step_project_beck_spring.entities;

import jakarta.persistence.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_read_status", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"thread_id", "user_id"})
})
public class ChatReadStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "thread_id")
    private ChatThread thread;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "last_read_message_id")
    private ChatMessage lastReadMessage;

    @UpdateTimestamp
    @Column(name = "read_at")
    private LocalDateTime readAt;

    public ChatReadStatus() {
    }

    public ChatReadStatus(ChatThread thread, User user, ChatMessage lastReadMessage) {
        this.thread = thread;
        this.user = user;
        this.lastReadMessage = lastReadMessage;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ChatThread getThread() {
        return thread;
    }

    public void setThread(ChatThread thread) {
        this.thread = thread;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public ChatMessage getLastReadMessage() {
        return lastReadMessage;
    }

    public void setLastReadMessage(ChatMessage lastReadMessage) {
        this.lastReadMessage = lastReadMessage;
    }

    public LocalDateTime getReadAt() {
        return readAt;
    }

    public void setReadAt(LocalDateTime readAt) {
        this.readAt = readAt;
    }
}

