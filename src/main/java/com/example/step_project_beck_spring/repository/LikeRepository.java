package com.example.step_project_beck_spring.repository;

import com.example.step_project_beck_spring.entities.Like;
import com.example.step_project_beck_spring.entities.Post;
import com.example.step_project_beck_spring.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface LikeRepository extends JpaRepository<Like, UUID> {
    Optional<Like> findByUserAndPost(User user, Post post);
    long countByPost(Post post);
}