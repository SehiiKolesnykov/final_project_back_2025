package com.example.step_project_beck_spring.repository;

import com.example.step_project_beck_spring.entities.ChatThread;
import com.example.step_project_beck_spring.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChatThreadRepository extends JpaRepository<ChatThread, UUID> {

    @Query("""
        SELECT DISTINCT t FROM ChatThread t 
        LEFT JOIN FETCH t.participants p 
        WHERE p = :user 
        ORDER BY CASE WHEN t.updatedAt IS NOT NULL THEN t.updatedAt ELSE t.createdAt END DESC
        """)
    List<ChatThread> findAllByParticipant(@Param("user") User user);

    @Query("""
            SELECT DISTINCT t FROM ChatThread t
            LEFT JOIN FETCH t.participants
            JOIN t.participants p1
            JOIN t.participants p2
            WHERE p1 = :user1 AND p2 = :user2
            """)
    Optional<ChatThread> findThreadBetween(@Param("user1") User user1, @Param("user2") User user2);
}