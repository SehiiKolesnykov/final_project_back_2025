package com.example.step_project_beck_spring.repository;

import com.example.step_project_beck_spring.entities.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface PostRepository extends JpaRepository<Post, UUID> {

    // Стрічка: тільки підписки + свої пости
    @Query("""
        SELECT p FROM Post p
        WHERE p.author.id IN (
            SELECT f.following.id FROM Follow f WHERE f.follower.id = :userId
        ) OR p.author.id = :userId
        ORDER BY p.createdAt DESC
        """)
    Page<Post> findFollowingPosts(@Param("userId") UUID userId, Pageable pageable);

    // Рекомендації: популярні пости від тих, на кого НЕ підписаний
    @Query("""
        SELECT p FROM Post p
        WHERE p.author.id NOT IN (
            SELECT f.following.id FROM Follow f WHERE f.follower.id = :userId
        ) AND p.author.id != :userId
        ORDER BY p.likesCount DESC, p.createdAt DESC
        """)
    Page<Post> findRecommendedPosts(@Param("userId") UUID userId, Pageable pageable);

    // Пости конкретного користувача (для профілю — публічний)
    Page<Post> findByAuthorIdOrderByCreatedAtDesc(UUID authorId, Pageable pageable);

    // Обрані пости
    @Query("""
        SELECT p FROM Post p
        JOIN SavedPost sp ON sp.post.id = p.id
        WHERE sp.user.id = :userId
        ORDER BY sp.savedAt DESC
        """)
    Page<Post> findSavedPosts(@Param("userId") UUID userId, Pageable pageable);
}