package com.example.step_project_beck_spring.repository;

import com.example.step_project_beck_spring.entities.ChatReadStatus;
import com.example.step_project_beck_spring.entities.ChatThread;
import com.example.step_project_beck_spring.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChatReadStatusRepository extends JpaRepository<ChatReadStatus, Long> {

    Optional<ChatReadStatus> findByThreadAndUser(ChatThread thread, User user);

    List<ChatReadStatus> findByThread(ChatThread thread);

    @Query("""
        SELECT COUNT(m) FROM ChatMessage m 
        WHERE m.thread = :thread 
        AND m.sender != :user
        AND (
            NOT EXISTS (
                SELECT r FROM ChatReadStatus r 
                WHERE r.thread = :thread AND r.user = :user
            ) 
            OR m.id > COALESCE((
                SELECT r.lastReadMessage.id 
                FROM ChatReadStatus r 
                WHERE r.thread = :thread AND r.user = :user
            ), NULL)
        )
        """)
    Long countUnreadMessages(@Param("thread") ChatThread thread, @Param("user") User user);
}