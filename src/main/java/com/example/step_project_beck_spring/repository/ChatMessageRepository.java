package com.example.step_project_beck_spring.repository;

import com.example.step_project_beck_spring.entities.ChatMessage;
import com.example.step_project_beck_spring.entities.ChatThread;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, UUID> {

    List<ChatMessage> findByThreadOrderByCreatedAtAsc(ChatThread thread);

    List<ChatMessage> findByThreadOrderByCreatedAtDesc(ChatThread thread, Pageable pageable);
    
    List<ChatMessage> findByThread(ChatThread thread);
}

