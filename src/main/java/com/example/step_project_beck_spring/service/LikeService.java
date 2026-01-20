package com.example.step_project_beck_spring.service;

import com.example.step_project_beck_spring.dto.LikeDto;
import com.example.step_project_beck_spring.entities.Like;
import com.example.step_project_beck_spring.entities.Post;
import com.example.step_project_beck_spring.entities.User;
import com.example.step_project_beck_spring.exeptions.PostNotFoundException;
import com.example.step_project_beck_spring.exeptions.UserNotFoundException;
import com.example.step_project_beck_spring.repository.LikeRepository;
import com.example.step_project_beck_spring.repository.PostRepository;
import com.example.step_project_beck_spring.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LikeService {

    private final LikeRepository likeRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    /**
     * toggleLike Якщо користувач вже лайкнув пост толайк видаляється.
     * Якщо ще не лайкнув то лайк додається.
     * LikeDto дає таку інформ:
     * postId та userId, чи лайкнуто (true/false),загальну кількість лайків
     */
    @Transactional
    public LikeDto toggleLike(UUID postId, UUID userId) {
        // Перевірка чи існує пост
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException(postId));
        // Перевірка чи існує користувач
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        // Пошук існуючого лайку від цього користувача для цього поста
        var existing = likeRepository.findByUserAndPost(user, post);

        // Чи новий лайк
        boolean nowLiked;


        if (existing.isPresent()) {
            // Якщо лайк є то видаляю
            likeRepository.delete(existing.get());
            nowLiked = false;
        } else {
            // Якщо лайку нема то створюємо новий
            Like like = Like.builder()
                    .post(post)
                    .user(user)
                    .build();
            likeRepository.save(like);
            nowLiked = true;
        }

        // Оновлюємо каунтер через приватний метод
        updatePostLikesCount(post);

        long currentCount = post.getLikesCount();  // вже оновлене значення

        return LikeDto.builder()
                .postId(postId)
                .userId(userId)
                .liked(nowLiked)
                .totalLikes(currentCount)
                .build();
    }

    /**
     * countLikes повертаємо кількість лайків для конкретного поста.
     */
    @Transactional(readOnly = true)
    public long countLikes(UUID postId) {
        // Перевірка чи існує пост
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException(postId));
        // Повертаємо кількість лайків
        return likeRepository.countByPost(post);
    }

    /**
     *  метод для оновлення каунтера поста
     */
    private void updatePostLikesCount(Post post) {
        long newCount = likeRepository.countByPost(post);
        post.setLikesCount((int) newCount);
        postRepository.save(post);
    }
}


