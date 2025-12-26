package com.example.step_project_beck_spring.repository;

import com.example.step_project_beck_spring.entities.ChatReadStatus;
import com.example.step_project_beck_spring.entities.ChatThread;
import com.example.step_project_beck_spring.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ChatReadStatusRepository extends JpaRepository<ChatReadStatus, Long> {

    Optional<ChatReadStatus> findByThreadAndUser(ChatThread thread, User user);

    @Query("""
        SELECT COUNT(m) FROM ChatMessage m 
        WHERE m.thread = :thread 
        AND m.sender != :user
        AND (
            NOT EXISTS (
                SELECT 1 FROM ChatReadStatus r 
                WHERE r.thread = :thread AND r.user = :user
            ) 
            OR EXISTS (
                SELECT 1 FROM ChatReadStatus r 
                WHERE r.thread = :thread 
                AND r.user = :user 
                AND m.id > r.lastReadMessage.id
            )
        )
        """)
    Long countUnreadMessages(@Param("thread") ChatThread thread, @Param("user") User user);
}

