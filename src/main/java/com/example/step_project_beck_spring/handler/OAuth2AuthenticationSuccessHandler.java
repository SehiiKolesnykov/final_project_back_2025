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

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final JwtService jwtService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        String googleId = oAuth2User.getAttribute("sub");
        String email = oAuth2User.getAttribute("email");
        String firstName = oAuth2User.getAttribute("given_name");
        String lastName = oAuth2User.getAttribute("family_name");
        String picture = oAuth2User.getAttribute("picture");

        // Знаходимо або створюємо користувача
        User user = userRepository.findByGoogleId(googleId)
                .orElseGet(() -> userRepository.findByEmail(email)
                        .map(existing -> {
                            // Якщо знайшли за email, але без googleId — прив'язуємо
                            existing.setGoogleId(googleId);
                            if (picture != null) existing.setAvatarUrl(picture);
                            return userRepository.save(existing);
                        })
                .orElseGet(() -> {
                    User newUser = User.builder()
                            .email(email)
                            .firstName(firstName != null ? firstName : "Google")
                            .lastName(lastName != null ? lastName : "User")
                            .avatarUrl(picture)
                            .firebaseUid(null)
                            .googleId(googleId)
                            .build();
                    return userRepository.save(newUser);
                }));

        // Генеруємо JWT (rememberMe = true → 7 днів)
        String jwt = jwtService.generateToken(user, true);

        // Ставимо HttpOnly cookie
        String cookie = "jwt=" + jwt + "; Path=/; HttpOnly; Secure; SameSite=Lax; Max-Age=604800";
        response.addHeader("Set-Cookie", cookie);

        // Перенаправляємо на фронт
        String redirectUri = (String) request.getSession().getAttribute("originalUrl");
        if (redirectUri == null) {
            redirectUri = "https://widi-rho.vercel.app/";
        }
        getRedirectStrategy().sendRedirect(request, response, redirectUri);
    }
}