package com.example.step_project_beck_spring.service;

import com.example.step_project_beck_spring.dto.UpdateUserRequest;
import com.example.step_project_beck_spring.dto.UserPublicDTO;
import com.example.step_project_beck_spring.entities.User;
import com.example.step_project_beck_spring.repository.FollowRepository;
import com.example.step_project_beck_spring.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;
    private final FollowRepository followRepository;

    @Override
    public UserPublicDTO getUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("User not found with id: " + id));
        return convertToDTO(user);
    }

    @Override
    public UserPublicDTO getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("User not found with email: " + email));
        //fetch для коректних лічильників
        user = userRepository.findByIdWithCollections(user.getId()).orElse(user);
        return convertToDTO(user);
    }

    @Override
    public void updateProfile(UpdateUserRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (request.firstName() != null) user.setFirstName(request.firstName());
        if (request.lastName() != null) user.setLastName(request.lastName());
        if (request.avatarUrl() != null) user.setAvatarUrl(request.avatarUrl());
        if (request.backgroundImgUrl() != null) user.setBackgroundImgUrl(request.backgroundImgUrl());
        if (request.birthDate() != null) user.setBirthDate(request.birthDate());

        userRepository.save(user);
    }

    // РУЧНИЙ МАПІНГ строго 9 полів як у DTO
    private UserPublicDTO convertToDTO(User user) {

        return new UserPublicDTO(
                user.getId(),                 // id
                user.getFirstName(),          // firstName
                user.getLastName(),           // lastName
                user.getEmail(),              // Email
                user.getAvatarUrl(),          // avatarUrl
                user.getBackgroundImgUrl(),   // backgroundImg (у DTO поле називається так у Entity - Url)
                user.getNickName() != null ? user.getNickName() : "TEST_NICK_FROM_CODE",
                user.getAboutMe(),
                user.getFollowing() != null ? user.getFollowing().size() : 0,     // followingCount (поки що 0)
                user.getFollowers() != null ? user.getFollowers().size() : 0,     // followersCount (поки що 0)
                user.getPosts() != null ? user.getPosts().size() : 0,             // postsCount (поки що 0)
                isFollowing(user)
        );
    }

    public Boolean isFollowing (User user) {
        User currentUser = currentUserService.getCurrentUser();

        if (currentUser.equals(user)) {
            return false;
        }
        boolean isFollowing = followRepository.existsByFollowerIdAndFollowingId(
                currentUser.getId(),
                user.getId()
        );

        return isFollowing;
    }
}