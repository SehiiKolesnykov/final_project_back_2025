package com.example.step_project_beck_spring.service;

import com.example.step_project_beck_spring.entities.User;
import com.example.step_project_beck_spring.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String email = oAuth2User.getAttribute("email");
        String firstName = oAuth2User.getAttribute("given_name");
        String lastName = oAuth2User.getAttribute("family_name");

        // Перевірка чи є такий юзер. Якщо немає — створюємо.
        userRepository.findByEmail(email).ifPresentOrElse(
                user -> {
                    // Якщо юзер вже є можна оновити йому ім'я/фото якщо треба
                },
                () -> {
                    User newUser = new User();
                    newUser.setEmail(email);
                    newUser.setFirstName(firstName != null ? firstName : "GoogleUser");
                    newUser.setLastName(lastName != null ? lastName : "");
                    // Генеруємо випадковий пароль, бо через Google пароль не потрібен
                    newUser.setPassword(UUID.randomUUID().toString());
                    // Тут встанови роль, дату народження та інші обов'язкові поля, якщо є
                    // newUser.setRole(Role.USER);
                    userRepository.save(newUser);
                }
        );

        return oAuth2User;
    }
}