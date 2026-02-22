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

        notificationSubscriptionService.notifySubscribersAboutNewPost(saved);

        return mapToDto(saved);
    }

    @Transactional
    public PostDto updatePost(UUID postId, UpdatePostRequest request) {
        UUID currentUserId = currentUserService.getCurrentUserId();
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Поста не знайдено"));

        if (!post.getAuthor().getId().equals(currentUserId)) {
            throw new IllegalArgumentException("Редагувати пост може лише його власник");
        }

        if (request.getContent() != null) post.setContent(request.getContent());
        if (request.getImageUrl() != null) post.setImageUrl(request.getImageUrl());

        Post updated = postRepository.save(post);
        return mapToDto(updated);
    }

    @Transactional
    public void deletePost(UUID postId) {
        UUID currentUserId = currentUserService.getCurrentUserId();
        if (!postRepository.existsByIdAndAuthorId(postId, currentUserId)) {
            throw new IllegalArgumentException("Видаляти пост може лише його власник або пост не існує");
        }
        postRepository.deleteById(postId);
    }

    @Transactional(readOnly = true)
    public Page<PostDto> getFeed(Pageable pageable) {
        UUID userId = currentUserService.getCurrentUserId();
        return postRepository.findFollowingPosts(userId, pageable)
                .map(this::mapToDto);
    }

    @Transactional(readOnly = true)
    public Page<PostDto> getRecommended(Pageable pageable) {
        UUID userId = currentUserService.getCurrentUserId();
        return postRepository.findRecommendedPosts(userId, pageable)
                .map(this::mapToDto);
    }

    @Transactional(readOnly = true)
    public PostDto getPostById(UUID id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Поста не знайдено"));
        return mapToDto(post);
    }

    @Transactional(readOnly = true)
    public Page<PostDto> getUserPosts(UUID userId, Pageable pageable) {
        return postRepository.findByAuthorId(userId, pageable)
                .map(this::mapToDto);
    }

    @Transactional
    public void savePost(UUID postId) {
        UUID userId = currentUserService.getCurrentUserId();
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

    @Transactional
    public void unsavePost(UUID postId) {
        UUID userId = currentUserService.getCurrentUserId();
        savedPostRepository.deleteByUserIdAndPostId(userId, postId);
    }

    @Transactional(readOnly = true)
    public Page<PostDto> getSavedPosts(Pageable pageable) {
        UUID userId = currentUserService.getCurrentUserId();
        return postRepository.findSavedPosts(userId, pageable)
                .map(this::mapToDto);
    }

    private PostDto mapToDto(Post post) {
        User currentUser = currentUserService.getCurrentUser();

        boolean isLiked = likeRepository.findByUserAndPost(currentUser, post).isPresent();
        boolean isSaved = savedPostRepository.existsByUserIdAndPostId(currentUser.getId(), post.getId());

        UserSummaryDto authorDto = new UserSummaryDto(
                post.getAuthor().getId(),
                post.getAuthor().getFirstName(),
                post.getAuthor().getLastName(),
                post.getAuthor().getAvatarUrl(),
                post.getAuthor().getNickName(),
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