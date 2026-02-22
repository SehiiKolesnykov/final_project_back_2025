package com.example.step_project_beck_spring.repository;

import com.example.step_project_beck_spring.entities.ChatMessage;
import com.example.step_project_beck_spring.entities.ChatThread;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, UUID> {

    @Query("SELECT m FROM ChatMessage m LEFT JOIN FETCH m.sender WHERE m.thread = :thread ORDER BY m.createdAt ASC")
    List<ChatMessage> findByThreadOrderByCreatedAtAsc(@Param("thread") ChatThread thread);

    @Query("SELECT m FROM ChatMessage m LEFT JOIN FETCH m.sender WHERE m.thread = :thread ORDER BY m.createdAt DESC")
    List<ChatMessage> findByThreadOrderByCreatedAtDesc(@Param("thread") ChatThread thread, Pageable pageable);

    @Query("SELECT m FROM ChatMessage m LEFT JOIN FETCH m.sender WHERE m.thread = :thread")
    List<ChatMessage> findByThread(@Param("thread") ChatThread thread);
}