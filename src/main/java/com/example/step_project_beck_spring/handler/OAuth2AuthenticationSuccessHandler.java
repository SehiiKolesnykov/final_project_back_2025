package com.example.step_project_beck_spring.handler;

import com.example.step_project_beck_spring.entities.User;
import com.example.step_project_beck_spring.repository.UserRepository;
import com.example.step_project_beck_spring.service.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final JwtService jwtService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        try {
            log.info("=== Початок обробки успішної Google авторизації ===");

            OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
            log.info("Отримано дані від Google: principal class = {}", oAuth2User.getClass().getName());

            String googleId = oAuth2User.getAttribute("sub");
            String email = oAuth2User.getAttribute("email");
            String firstName = oAuth2User.getAttribute("given_name");
            String lastName = oAuth2User.getAttribute("family_name");
            String picture = oAuth2User.getAttribute("picture");

            log.info("Google дані: googleId={}, email={}, name={} {}, picture={}",
                    googleId, email, firstName, lastName, picture);

            if (email == null) {
                log.error("Email від Google відсутній");
                response.sendError(500, "Email не надано Google");
                return;
            }

            User user = userRepository.findByGoogleId(googleId)
                    .orElseGet(() -> userRepository.findByEmail(email)
                            .map(existing -> {
                                log.info("Знайдено користувача за email, додаємо googleId");
                                existing.setGoogleId(googleId);
                                if (picture != null) existing.setAvatarUrl(picture);
                                return userRepository.save(existing);
                            })
                            .orElseGet(() -> {
                                log.info("Створюємо нового користувача через Google");

                                String nickName = email.substring(0, email.indexOf('@'));
                                if (nickName.length() > 19) {
                                    nickName = nickName.substring(0, 19);
                                }

                                // Перевіряємо унікальність
                                int counter = 1;
                                String baseNick = nickName;
                                while (userRepository.existsByNickName(nickName)) {
                                    nickName = baseNick + "_" + counter++;
                                    if (nickName.length() > 20) {
                                        nickName = nickName.substring(0, 20);
                                    }
                                }

                                User newUser = User.builder()
                                        .googleId(googleId)
                                        .email(email)
                                        .firstName(firstName != null ? firstName : "Google")
                                        .lastName(lastName != null ? lastName : "User")
                                        .avatarUrl(picture)
                                        .nickName(nickName)
                                        .birthDate(null)
                                        .firebaseUid(null)
                                        .aboutMe(null)
                                        .createdAt(LocalDateTime.now())
                                        .build();

                                return userRepository.save(newUser);
                            }));

            log.info("Користувач готовий: id={}, email={}, nickName={}", user.getId(), user.getEmail(), user.getNickName());

            String jwt = jwtService.generateToken(user, true);
            log.info("JWT успішно згенеровано");

            String cookie = "jwt=" + jwt + "; Path=/; HttpOnly; Secure; SameSite=None; Max-Age=604800";
            response.addHeader("Set-Cookie", cookie);
            log.info("Cookie встановлено");

            String redirectUrl = "https://widi-rho.vercel.app/auth";
            log.info("Редирект на: {}", redirectUrl);
            getRedirectStrategy().sendRedirect(request, response, redirectUrl);

        } catch (Exception e) {
            log.error("Помилка в OAuth2 success handler", e);
            response.sendError(500, "Помилка авторизації: " + e.getMessage());
        }
    }
}