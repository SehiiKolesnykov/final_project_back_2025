package com.example.step_project_beck_spring.service;

import com.example.step_project_beck_spring.dto.UserSummaryDto;
import com.example.step_project_beck_spring.entities.Follow;
import com.example.step_project_beck_spring.entities.User;
import com.example.step_project_beck_spring.mapper.UserMapper;
import com.example.step_project_beck_spring.repository.FollowRepository;
import com.example.step_project_beck_spring.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
@Service
@RequiredArgsConstructor
public class FollowService {
    private final FollowRepository followRepository;
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    //Підписка одного користувача на іншого.
    @Transactional
    public void follow(UUID followerId, UUID targetUserId) {
        if (followerId.equals(targetUserId)) {
            throw new IllegalArgumentException("Не можна підписатися на самого себе");
        }
        // Перевірка, чи вже є підписка
        if (followRepository.existsByFollowerIdAndFollowingId(followerId, targetUserId)) {
            return;
        }
        User follower = userRepository.findById(followerId)
                .orElseThrow(() -> new IllegalArgumentException("Користувача (follower) не знайдено"));

        User following = userRepository.findById(targetUserId)
                .orElseThrow(() -> new IllegalArgumentException("Користувача (following) не знайдено"));

        Follow follow = Follow.builder()
                .follower(follower)
                .following(following)
                .build();

        followRepository.save(follow);
    }
    //Відписка користувача від іншого користувача.
    @Transactional
    public void unfollow(UUID followerId, UUID targetUserId) {
        followRepository.deleteByFollowerIdAndFollowingId(followerId, targetUserId);
    }
    // Перевірка, чи followerId підписаний на targetUserId.
    @Transactional(readOnly = true)
    public boolean isFollowing(UUID followerId, UUID targetUserId) {
        return followRepository.existsByFollowerIdAndFollowingId(followerId, targetUserId);
    }
    //Список користувачів, на яких підписаний userId (following).

    @Transactional(readOnly = true)
    public List<UserSummaryDto> getFollowing(UUID userId) {
        List<Follow> following = followRepository.findByFollowerId(userId);
        return following.stream()
                .map(Follow::getFollowing)
                .map(userMapper::toUserSummary)
                .toList();
    }
    //Список користувачів, які підписані на userId (followers).
    @Transactional(readOnly = true)
    public List<UserSummaryDto> getFollowers(UUID userId) {
        List<Follow> followers = followRepository.findByFollowingId(userId);
        return followers.stream()
                .map(Follow::getFollower)
                .map(userMapper::toUserSummary)
                .toList();
    }
    //Кількість підписників користувача.
    @Transactional(readOnly = true)
    public long countFollowers(UUID userId) {
        return followRepository.countByFollowingId(userId);
    }
    //Кількість підписок користувача.
    @Transactional(readOnly = true)
    public long countFollowing(UUID userId) {
        return followRepository.countByFollowerId(userId);
    }
}


