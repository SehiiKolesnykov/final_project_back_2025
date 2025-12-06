package com.example.step_project_beck_spring.service;

import com.example.step_project_beck_spring.dto.CommentDto;
import com.example.step_project_beck_spring.entities.Comment;
import com.example.step_project_beck_spring.entities.Post;
import com.example.step_project_beck_spring.entities.User;
import com.example.step_project_beck_spring.exeptions.PostNotFoundException;
import com.example.step_project_beck_spring.exeptions.UserNotFoundException;
import com.example.step_project_beck_spring.repository.CommentRepository;
import com.example.step_project_beck_spring.repository.PostRepository;
import com.example.step_project_beck_spring.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Transactional
    public CommentDto addComment(UUID postId, UUID userId, String content) {
        // Перевірка чи існує пост
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException(postId));
        // Перевірка чи існує користувач
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        // Створення нового коментаря
        Comment comment = Comment.builder()
                .post(post)
                .author(user)
                .content(content)
                .build();

        // Збереження коментаря в БД
        Comment saved = commentRepository.save(comment);

        return new CommentDto(
                saved.getId(),
                saved.getContent(),
                saved.getAuthor().getId(),
                saved.getPost().getId(),
                saved.getCreatedAt()
        );
    }

    /**
     * getComments всі коментарі для конкретного поста.
     * - Перевіряємо, чи є пост
     * - Завантажує коментарі з БД спочвтку нові потім старі
     */
    @Transactional(readOnly = true)
    public List<CommentDto> getComments(UUID postId) {
        // Перевірка чи існує пост
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException(postId));

        // Завантаження та мапінг коментарів у DTO
        return commentRepository.findByPostOrderByCreatedAtDesc(post)
                .stream()
                .map(c -> new CommentDto(
                        c.getId(),
                        c.getContent(),
                        c.getAuthor().getId(),
                        c.getPost().getId(),
                        c.getCreatedAt()
                ))
                .toList();
    }
}



