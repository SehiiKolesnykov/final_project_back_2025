package com.example.step_project_beck_spring.config;

import com.example.step_project_beck_spring.service.JwtService;
import com.example.step_project_beck_spring.entities.User;
import com.example.step_project_beck_spring.repository.UserRepository;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class JwtStompChannelInterceptor implements ChannelInterceptor {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    public JwtStompChannelInterceptor(JwtService jwtService, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        log.info("=== STOMP preSend START ===");
        log.info("Command: {}", accessor.getCommand());

        if (StompCommand.CONNECT.equals(accessor.getCommand()) ||
                StompCommand.SEND.equals(accessor.getCommand())) {

            String authHeader = accessor.getFirstNativeHeader("Authorization");
            log.info("Authorization header: {}", authHeader);

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.error("Authorization header missing or invalid for command: {}", accessor.getCommand());
                throw new IllegalStateException("Authorization header required for WebSocket");
            }

            String token = authHeader.substring(7);
            log.info("Token extracted (first 30 chars): {}", token.substring(0, Math.min(30, token.length())));

            try {
                String username = jwtService.extractUsername(token);
                log.info("Username from token: {}", username);

                User user = userRepository.findByEmail(username).orElse(null);
                if (user == null) {
                    log.error("User not found for email: {}", username);
                    throw new IllegalArgumentException("User not found");
                }

                boolean valid = jwtService.validateToken(token, user);
                log.info("Token validation result: {}", valid);

                if (!valid) {
                    log.error("Invalid token for user: {}", username);
                    throw new IllegalStateException("Invalid JWT token");
                }

                // Створюємо автентифікацію
                Authentication auth = new UsernamePasswordAuthenticationToken(
                        user, null, user.getAuthorities()
                );

                // Очищаємо контекст (важливо для багатопоточності WebSocket)
                SecurityContextHolder.clearContext();
                SecurityContextHolder.getContext().setAuthentication(auth);

                // Передаємо повний Authentication як Principal
                accessor.setUser(auth);

                log.info("SUCCESS: Authenticated user {} for command {}", user.getEmail(), accessor.getCommand());

            } catch (Exception e) {
                log.error("FAILED: WebSocket auth error for command {}: {}", accessor.getCommand(), e.getMessage(), e);
                throw new IllegalStateException("WebSocket authentication failed: " + e.getMessage(), e);
            }
        }

        log.info("=== STOMP preSend END ===");

        return message;
    }
}