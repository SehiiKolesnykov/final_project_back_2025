// src/main/java/com/example/step_project_beck_spring/controller/LogoutController.java
package com.example.step_project_beck_spring.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class LogoutController {

    private static final String FRONTEND_HOME = "https://widi-rho.vercel.app/";

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        // Створюємо куку, яка "видаляє" jwt
        ResponseCookie deleteCookie = ResponseCookie.from("jwt", "")
                .httpOnly(true)
                .secure(true)                    // обов'язково для HTTPS (Render)
                .sameSite("None")                // для крос-доменних запитів з Vercel
                .path("/")
                .maxAge(0)                       // одразу видаляємо
                .build();

        // Додаємо заголовок Set-Cookie, який видалить куку в браузері
        response.addHeader(HttpHeaders.SET_COOKIE, deleteCookie.toString());

        // Для API повертаємо 204 No Content

        return ResponseEntity.noContent().build();
    }
}
