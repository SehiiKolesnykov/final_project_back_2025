package com.example.step_project_beck_spring.repository;

import com.example.step_project_beck_spring.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** JpaRepository для отримання базових CRUD-методів */
public interface UserRepository extends JpaRepository<User, UUID> {

    /** Spring Data JPA автоматично генерує SQL-запит (SELECT FROM users WHERE email = ?) */
    Optional<User> findByEmail(String email);

    /**
     * Пошук користувачів за ім'ям або email (case-insensitive).
     * Виключає поточного користувача з результатів.
     */
    @Query("SELECT u FROM User u WHERE " +
           "u.id != :currentUserId AND " +
           "(LOWER(u.firstName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(CONCAT(u.firstName, ' ', u.lastName)) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<User> searchUsers(@Param("query") String query, @Param("currentUserId") UUID currentUserId);

    /**
     * Завантаження користувача з колекціями для отримання розмірів.
     * Використовує JOIN FETCH для завантаження followers, following та posts в одному запиті.
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