package com.example.step_project_beck_spring.repository;

import com.example.step_project_beck_spring.entities.Follow;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface FollowRepository extends JpaRepository<Follow, UUID> {

    boolean existsByFollowerIdAndFollowingId(UUID followerId, UUID followingId);

    void deleteByFollowerIdAndFollowingId(UUID followerId, UUID followingId);

    List<Follow> findByFollowerId(UUID followerId); // На кого підписаний користувач

    List<Follow> findByFollowingId(UUID followingId); // Хто підписаний на користувача

    long countByFollowerId(UUID followerId);  // Кількість підписок

    long countByFollowingId(UUID followingId); // Кількість підписників
}


