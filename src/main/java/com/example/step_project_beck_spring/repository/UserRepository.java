package com.example.step_project_beck_spring.repository;

import com.example.step_project_beck_spring.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Репозиторій для сутності User
 */
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    Optional<User> findByFirebaseUid(String firebaseUid);

    Optional<User> findByGoogleId(String googleId);

    /**
     * Пошук користувачів за частиною імені, прізвища або email (case-insensitive)
     * Виключає поточного користувача
     */
    @Query("""
        SELECT u FROM User u 
        WHERE u.id != :currentUserId 
          AND (
              LOWER(u.firstName) LIKE LOWER(CONCAT('%', :query, '%')) OR 
              LOWER(u.lastName) LIKE LOWER(CONCAT('%', :query, '%')) OR 
              LOWER(CONCAT(u.firstName, ' ', u.lastName)) LIKE LOWER(CONCAT('%', :query, '%')) OR 
              LOWER(u.email) LIKE LOWER(CONCAT('%', :query, '%'))
          )
        """)
    List<User> searchUsers(@Param("query") String query, @Param("currentUserId") UUID currentUserId);

    /**
     * Знаходить користувача разом із завантаженими колекціями (followers, following, posts)
     */
    @Query("""
        SELECT DISTINCT u FROM User u
        LEFT JOIN FETCH u.followers
        LEFT JOIN FETCH u.following
        LEFT JOIN FETCH u.posts
        WHERE u.id = :userId
        """)
    Optional<User> findByIdWithCollections(@Param("userId") UUID userId);
}