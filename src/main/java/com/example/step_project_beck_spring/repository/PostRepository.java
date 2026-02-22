package com.example.step_project_beck_spring.repository;

import com.example.step_project_beck_spring.entities.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

/**
 * Репозиторій для роботи з сутністю Post
 */
public interface PostRepository extends JpaRepository<Post, UUID> {

    @Query("""
        SELECT p FROM Post p
        WHERE p.author.id IN (
            SELECT f.following.id FROM Follow f WHERE f.follower.id = :userId
        ) OR p.author.id = :userId
        """)
    Page<Post> findFollowingPosts(@Param("userId") UUID userId, Pageable pageable);

    @Query("""
        SELECT p FROM Post p
        WHERE p.author.id NOT IN (
            SELECT f.following.id FROM Follow f WHERE f.follower.id = :userId
        ) AND p.author.id != :userId
        """)
    Page<Post> findRecommendedPosts(@Param("userId") UUID userId, Pageable pageable);

    Page<Post> findByAuthorId(UUID authorId, Pageable pageable);

    @Query("""
        SELECT p FROM Post p
        JOIN SavedPost sp ON sp.post.id = p.id
        WHERE sp.user.id = :userId
        """)
    Page<Post> findSavedPosts(@Param("userId") UUID userId, Pageable pageable);

    boolean existsByIdAndAuthorId(UUID id, UUID authorId);
}