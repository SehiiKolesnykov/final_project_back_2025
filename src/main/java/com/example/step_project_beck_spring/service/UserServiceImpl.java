package com.example.step_project_beck_spring.service;

import com.example.step_project_beck_spring.dto.UserPublicDTO;
import com.example.step_project_beck_spring.repository.UserRepository;
import com.example.step_project_beck_spring.entities.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.UUID;
import java.util.NoSuchElementException;

/** Реалізація логіки для користувачів */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    /** Ввод репозиторію для доступу до БД */
    private final UserRepository userRepository;

    @Override
    public UserPublicDTO getUserById(UUID id) {
        /** Шукаємо користувача якщо об'єкт порожній, кидаємо нот фаунд */
        User user = userRepository.findById(id).orElseThrow(() -> new NoSuchElementException("User not found with id: " + id));
        return convertToPublicDTO(user);
    }
    @Override
    public UserPublicDTO getUserByEmail(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new NoSuchElementException("User not found with email: " + email));
        return convertToPublicDTO(user);
    }

    /** конвертація User Entity в UserPublicDTO */
    private UserPublicDTO convertToPublicDTO(User user) {
        UserPublicDTO dto = new UserPublicDTO();

        /** відображення базових полів */
        dto.setId(user.getId());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setAvatarUrl(user.getAvatarUrl());
        /** не працює без БД */
        //dto.setBackgroundImg(user.getBackgroundImg());

        /** лічильники, далі потрібно зробити логіку для підрахуноку в БД: підпісників,підписок,постів */
        dto.setFollowersCount(0);
        dto.setFollowingCount(0);
        dto.setPostsCount(0);
        /** не працює без БД */
        //dto.setIsFollowing(false);
        return dto;
    }
}