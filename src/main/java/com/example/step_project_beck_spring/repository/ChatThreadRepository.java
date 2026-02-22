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

    /**
     * Знаходить усі треди, де користувач є учасником,
     * і завантажує ВСІХ учасників (не тільки поточного)
     */
    @Query("""
        SELECT t FROM ChatThread t
        LEFT JOIN FETCH t.participants
        WHERE :user MEMBER OF t.participants
        ORDER BY COALESCE(t.updatedAt, t.createdAt) DESC
        """)
    List<ChatThread> findAllByParticipant(@Param("user") User user);

    /**
     * Знаходить тред між двома користувачами,
     * завантажує всіх учасників
     */
    @Query("""
        SELECT t FROM ChatThread t
        LEFT JOIN FETCH t.participants
        WHERE :user1 MEMBER OF t.participants
        AND :user2 MEMBER OF t.participants
        """)
    Optional<ChatThread> findThreadBetween(@Param("user1") User user1, @Param("user2") User user2);
}