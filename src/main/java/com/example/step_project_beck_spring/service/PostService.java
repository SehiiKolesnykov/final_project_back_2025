package com.example.step_project_beck_spring.service;

import com.example.step_project_beck_spring.dto.PostDto;
import com.example.step_project_beck_spring.dto.UserSummaryDto;
import com.example.step_project_beck_spring.entities.Post;
import com.example.step_project_beck_spring.entities.SavedPost;
import com.example.step_project_beck_spring.entities.User;
import com.example.step_project_beck_spring.repository.LikeRepository;
import com.example.step_project_beck_spring.repository.PostRepository;
import com.example.step_project_beck_spring.repository.SavedPostRepository;
import com.example.step_project_beck_spring.repository.UserRepository;
import com.example.step_project_beck_spring.request.CreatePostRequest;
import com.example.step_project_beck_spring.request.UpdatePostRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Сервісний шар для роботи з постами
 * Містить бізнес-логіку створення, редагування, видалення, отримання стрічки,
 * рекомендацій, збережених постів тощо
 */
@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final SavedPostRepository savedPostRepository;
    private final NotificationSubscriptionService notificationSubscriptionService;
    private final CurrentUserService currentUserService;
    private final LikeRepository likeRepository;
    private final UserService userService;

    /**
     * Створює новий пост від імені поточного користувача
     *
     * @param request дані для створення поста (вміст + опціональне зображення)
     * @return DTO створеного поста
     * @throws IllegalArgumentException якщо автора не знайдено
     */
    @Transactional
    public PostDto createPost(CreatePostRequest request) {
        UUID authorId = currentUserService.getCurrentUserId();
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new IllegalArgumentException("Автор не знайдено"));

        Post post = Post.builder()
                .content(request.getContent())
                .imageUrl(request.getImageUrl())
                .author(author)
                .build();

        Post saved = postRepository.save(post);
        postRepository.flush();

        // Надсилаємо сповіщення підписникам про новий пост
        notificationSubscriptionService.notifySubscribersAboutNewPost(saved);

        return mapToDto(saved);
    }

    /**
     * Частково оновлює існуючий пост (тільки власник може змінювати)
     *
     * @param postId  ідентифікатор поста
     * @param request поля, які потрібно оновити (можуть бути null)
     * @return оновлений пост у вигляді DTO
     * @throws IllegalArgumentException якщо пост не знайдено або користувач не є власником
     */
    @Transactional
    public PostDto updatePost(UUID postId, UpdatePostRequest request) {
        UUID currentUserId = currentUserService.getCurrentUserId();
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Поста не знайдено"));

        if (!post.getAuthor().getId().equals(currentUserId)) {
            throw new IllegalArgumentException("Редагувати пост може лише його власник");
        }

        // Оновлюємо лише ті поля, які передані (null = не змінювати)
        if (request.getContent() != null) {
            post.setContent(request.getContent());
        }
        if (request.getImageUrl() != null) {
            post.setImageUrl(request.getImageUrl());
        }

        Post updated = postRepository.save(post);
        return mapToDto(updated);
    }

    /**
     * Видаляє пост (тільки власник може видаляти)
     *
     * @param postId ідентифікатор поста для видалення
     * @throws IllegalArgumentException якщо пост не існує або користувач не є власником
     */
    @Transactional
    public void deletePost(UUID postId) {
        UUID currentUserId = currentUserService.getCurrentUserId();

        if (!postRepository.existsByIdAndAuthorId(postId, currentUserId)) {
            throw new IllegalArgumentException("Видаляти пост може лише його власник або пост не існує");
        }

        postRepository.deleteById(postId);
    }

    /**
     * Отримує персоналізовану стрічку постів (feed) поточного користувача
     * Включає пости від людей, на яких підписаний + власні пости
     *
     * @param pageable параметри пагінації та сортування
     * @return сторінка постів
     */
    @Transactional(readOnly = true)
    public Page<PostDto> getFeed(Pageable pageable) {
        UUID userId = currentUserService.getCurrentUserId();
        return postRepository.findFollowingPosts(userId, pageable)
                .map(this::mapToDto);
    }

    /**
     * Отримує рекомендовані пости для поточного користувача
     * (пости від людей, на яких не підписаний, сортовані за популярністю)
     *
     * @param pageable параметри пагінації
     * @return сторінка рекомендованих постів
     */
    @Transactional(readOnly = true)
    public Page<PostDto> getRecommended(Pageable pageable) {
        UUID userId = currentUserService.getCurrentUserId();
        return postRepository.findRecommendedPosts(userId, pageable)
                .map(this::mapToDto);
    }

    /**
     * Отримує один конкретний пост за ідентифікатором
     *
     * @param id ідентифікатор поста
     * @return DTO поста
     * @throws IllegalArgumentException якщо пост не знайдено
     */
    @Transactional(readOnly = true)
    public PostDto getPostById(UUID id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Поста не знайдено"));
        return mapToDto(post);
    }

    /**
     * Отримує пости конкретного користувача (для профілю)
     *
     * @param userId   ідентифікатор автора
     * @param pageable параметри пагінації
     * @return сторінка постів користувача
     */
    @Transactional(readOnly = true)
    public Page<PostDto> getUserPosts(UUID userId, Pageable pageable) {
        return postRepository.findByAuthorIdOrderByCreatedAtDesc(userId, pageable)
                .map(this::mapToDto);
    }

    /**
     * Додає пост до збережених (закладок) поточного користувача
     * Ідемпотентна операція — повторне збереження того ж поста ігнорується
     *
     * @param postId ідентифікатор поста
     */
    @Transactional
    public void savePost(UUID postId) {
        UUID userId = currentUserService.getCurrentUserId();

        // Ідемпотентність: якщо вже збережено — нічого не робимо
        if (savedPostRepository.existsByUserIdAndPostId(userId, postId)) {
            return;
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Користувача не знайдено"));

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Поста не знайдено"));

        SavedPost savedPost = SavedPost.builder()
                .user(user)
                .post(post)
                .build();

        savedPostRepository.save(savedPost);
    }

    /**
     * Видаляє пост зі збережених (закладок) поточного користувача
     * Ідемпотентна операція — якщо пост не був збережений, нічого не відбувається
     *
     * @param postId ідентифікатор поста
     */
    @Transactional
    public void unsavePost(UUID postId) {
        UUID userId = currentUserService.getCurrentUserId();
        savedPostRepository.deleteByUserIdAndPostId(userId, postId);
    }

    /**
     * Отримує список збережених постів поточного користувача
     *
     * @param pageable параметри пагінації
     * @return сторінка збережених постів
     */
    @Transactional(readOnly = true)
    public Page<PostDto> getSavedPosts(Pageable pageable) {
        UUID userId = currentUserService.getCurrentUserId();
        return postRepository.findSavedPosts(userId, pageable)
                .map(this::mapToDto);
    }

    /**
     * Перетворює сутність Post у DTO для передачі клієнту
     *
     * @param post сутність поста з БД
     * @return готовий до відправки DTO
     */
    private PostDto mapToDto(Post post) {
        User currentUser = currentUserService.getCurrentUser();

        boolean isLiked = likeRepository.findByUserAndPost(currentUser, post).isPresent();
        boolean isSaved = savedPostRepository.existsByUserIdAndPostId(currentUser.getId(), post.getId());

        UserSummaryDto authorDto = new UserSummaryDto(
                post.getAuthor().getId(),
                post.getAuthor().getFirstName(),
                post.getAuthor().getLastName(),
                post.getAuthor().getAvatarUrl(),
                userService.isFollowing(post.getAuthor())
        );

        return PostDto.builder()
                .id(post.getId())
                .content(post.getContent())
                .imageUrl(post.getImageUrl())
                .author(authorDto)
                .createdAt(post.getCreatedAt())
                .likesCount(post.getLikesCount())
                .commentsCount(post.getCommentsCount())
                .repostsCount(post.getRepostsCount())
                .quotesCount(post.getQuotesCount())
                .liked(isLiked)
                .saved(isSaved)
                .build();
    }
}