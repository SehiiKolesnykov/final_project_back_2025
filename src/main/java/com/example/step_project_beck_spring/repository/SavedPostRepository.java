package com.example.step_project_beck_spring.repository;

import com.example.step_project_beck_spring.entities.SavedPost;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * Репозиторій для роботи з сутністю SavedPost
 * Зберігає інформацію про пости, які користувач додав у закладки (saved/bookmarked)
 */
public interface SavedPostRepository extends JpaRepository<SavedPost, UUID> {

    /**
     * Перевіряє, чи доданий конкретний пост у закладки певним користувачем
     *
     * @param userId ідентифікатор користувача
     * @param postId ідентифікатор поста
     * @return true — якщо пост вже збережений цим користувачем
     */
    boolean existsByUserIdAndPostId(UUID userId, UUID postId);

    /**
     * Видаляє запис про збереження поста конкретним користувачем
     * (тобто видаляє пост із закладок користувача)
     *
     * @param userId ідентифікатор користувача
     * @param postId ідентифікатор поста, який потрібно видалити з закладок
     */
    void deleteByUserIdAndPostId(UUID userId, UUID postId);
}