package com.example.step_project_beck_spring.repository;

import com.example.step_project_beck_spring.entities.Comment;
import com.example.step_project_beck_spring.entities.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CommentRepository extends JpaRepository<Comment, UUID> {
    List<Comment> findByPostOrderByCreatedAtDesc(Post post);
}
