//package com.example.step_project_beck_spring.config;
//
//import com.example.step_project_beck_spring.entities.User;
//import com.example.step_project_beck_spring.repository.UserRepository;
//import com.example.step_project_beck_spring.service.JwtService;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import lombok.RequiredArgsConstructor;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.oauth2.core.user.OAuth2User;
//import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
//import org.springframework.stereotype.Component;
//import org.springframework.web.util.UriComponentsBuilder;
//
//import java.io.IOException;
//
//@Component
//@RequiredArgsConstructor
//public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
//
//    private final JwtService jwtService;
//    private final UserRepository userRepository;
//
//    // Адреса фронтенду (краще винести в properties)
//    @Value("${application.frontend-url}")
//    private String frontendBaseUrl;
//
//    @Override
//    public void onAuthenticationSuccess(HttpServletRequest request,
//                                        HttpServletResponse response,
//                                        Authentication authentication) throws IOException, ServletException {
//
//        // Дістаємо email користувача з даних Google
//        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
//        String email = oAuth2User.getAttribute("email");
//
//        // Знаходимо користувача в базі (він там вже є завдяки CustomOAuth2UserService)
//        User user = userRepository.findByEmail(email)
//                .orElseThrow(() -> new RuntimeException("User not found after OAuth2 login"));
//
//        // Генеруємо токен за допомогою твого JwtService
//        String jwtToken = jwtService.generateToken(user, true);
//
//        // Формуємо посилання на фронтенд і додаємо токен як параметр
//        // Додаємо шлях /oauth2/callback до базового URL
//        String targetUrl = UriComponentsBuilder.fromUriString(frontendBaseUrl + "/oauth2/callback")
//                .queryParam("token", jwtToken)
//                .build().toUriString();
//        // Перенаправляємо користувача на це посилання
//        this.getRedirectStrategy().sendRedirect(request, response, targetUrl);
//    }
//}