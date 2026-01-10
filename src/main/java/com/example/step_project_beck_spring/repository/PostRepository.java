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
 * Містить як стандартні методи JpaRepository, так і кастомні запити
 */
public interface PostRepository extends JpaRepository<Post, UUID> {

    /**
     * Отримує стрічку постів поточного користувача (feed)
     * Включає:
     * - пости людей, на яких підписаний користувач
     * - власні пости користувача
     *
     * @param userId   ідентифікатор поточного користувача
     * @param pageable параметри пагінації та сортування
     * @return сторінка постів у хронологічному порядку (найновіші першими)
     */
    @Query("""
        SELECT p FROM Post p
        WHERE p.author.id IN (
            SELECT f.following.id FROM Follow f WHERE f.follower.id = :userId
        ) OR p.author.id = :userId
        ORDER BY p.createdAt DESC
        """)
    Page<Post> findFollowingPosts(@Param("userId") UUID userId, Pageable pageable);

    /**
     * Отримує рекомендовані пости для користувача
     * Критерії рекомендації:
     * - пости від користувачів, на яких поточний користувач НЕ підписаний
     * - виключаються власні пости
     * Сортування: спочатку за кількістю лайків (від більшого), потім за датою
     *
     * @param userId   ідентифікатор поточного користувача
     * @param pageable параметри пагінації
     * @return сторінка рекомендованих постів
     */
    @Query("""
        SELECT p FROM Post p
        WHERE p.author.id NOT IN (
            SELECT f.following.id FROM Follow f WHERE f.follower.id = :userId
        ) AND p.author.id != :userId
        ORDER BY p.likesCount DESC, p.createdAt DESC
        """)
    Page<Post> findRecommendedPosts(@Param("userId") UUID userId, Pageable pageable);

    /**
     * Знаходить всі пости конкретного користувача
     * Використовується для відображення на сторінці профілю
     *
     * @param authorId ідентифікатор автора постів
     * @param pageable параметри пагінації
     * @return сторінка постів у зворотному хронологічному порядку
     */
    Page<Post> findByAuthorIdOrderByCreatedAtDesc(UUID authorId, Pageable pageable);

    /**
     * Отримує список збережених (закладкових) постів користувача
     *
     * @param userId   ідентифікатор користувача
     * @param pageable параметри пагінації
     * @return сторінка збережених постів, відсортованих за часом збереження (найновіші першими)
     */
    @Query("""
        SELECT p FROM Post p
        JOIN SavedPost sp ON sp.post.id = p.id
        WHERE sp.user.id = :userId
        ORDER BY sp.savedAt DESC
        """)
    Page<Post> findSavedPosts(@Param("userId") UUID userId, Pageable pageable);

    /**
     * Перевіряє, чи належить пост з вказаним id конкретному автору
     * Зазвичай використовується для перевірки прав на редагування/видалення
     *
     * @param id       ідентифікатор поста
     * @param authorId ідентифікатор автора
     * @return true — якщо пост існує і належить цьому автору
     */
    boolean existsByIdAndAuthorId(UUID id, UUID authorId);
}