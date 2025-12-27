package com.example.step_project_beck_spring.service;

import com.example.step_project_beck_spring.entities.Post;
import com.example.step_project_beck_spring.entities.User;
import com.example.step_project_beck_spring.repository.PostRepository;
import com.example.step_project_beck_spring.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

/**
 * Сервіс створення постів, валідацію автора,
 * збереження поста та запуск сповіщень для підписників.
 */
@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final NotificationSubscriptionService notificationSubscriptionService;

    /**
     * Створення нового поста.
     * @param authorId — ID автора (береться з JWT)
     * @param content — текст поста
     * @param imageUrl — опціональна картинка
     * @return збережений пост
     */
    @Transactional
    public Post createPost(UUID authorId, String content, String imageUrl) {
        //Перевіряємо, що автор існує
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new IllegalArgumentException("Автор поста не знайдений"));
        //Створюємо новий пост
        Post post = Post.builder()
                .author(author)
                .content(content)
                .imageUrl(imageUrl)
                .build();
        //Зберігаємо пост у базу
        Post savedPost = postRepository.save(post);

        //після створення поста — розсилаємо сповіщення всім підписникам
        notificationSubscriptionService.notifySubscribersAboutNewPost(savedPost);

        //Повертаємо збережений пост
        return savedPost;
    }
}


