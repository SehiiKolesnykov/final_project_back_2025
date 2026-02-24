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

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader("Authorization");
            log.info("Authorization header at CONNECT: {}", authHeader);

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.error("Authorization header missing at CONNECT");
                throw new IllegalStateException("Authorization header required for CONNECT");
            }

            String token = authHeader.substring(7);
            log.info("Token at CONNECT: {}...", token.substring(0, Math.min(30, token.length())));

            try {
                String username = jwtService.extractUsername(token);
                log.info("Username from token at CONNECT: {}", username);

                User user = userRepository.findByEmail(username).orElse(null);
                if (user == null) {
                    log.error("User not found at CONNECT: {}", username);
                    throw new IllegalArgumentException("User not found");
                }

                boolean valid = jwtService.validateToken(token, user);
                log.info("Token validation at CONNECT: {}", valid);

                if (!valid) {
                    log.error("Invalid token at CONNECT for user: {}", username);
                    throw new IllegalStateException("Invalid JWT token");
                }

                // Зберігаємо користувача в сесії STOMP (для всіх майбутніх SEND)
                accessor.getSessionAttributes().put("currentUser", user);

                // Опціонально: встановлюємо контекст для CONNECT
                Authentication auth = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(auth);

                log.info("SUCCESS CONNECT: User {} saved in STOMP session", user.getEmail());

            } catch (Exception e) {
                log.error("FAILED CONNECT: {}", e.getMessage(), e);
                throw new IllegalStateException("CONNECT authentication failed: " + e.getMessage(), e);
            }

        } else if (StompCommand.SEND.equals(accessor.getCommand())) {
            // Витягуємо користувача з сесії STOMP
            User currentUser = (User) accessor.getSessionAttributes().get("currentUser");

            if (currentUser == null) {
                log.error("No user found in STOMP session for SEND");
                throw new IllegalStateException("User not found in STOMP session");
            }

            log.info("SUCCESS SEND: Loaded user {} from STOMP session", currentUser.getEmail());

            // Встановлюємо контекст для контролера (якщо потрібно)
            Authentication auth = new UsernamePasswordAuthenticationToken(
                    currentUser, null, currentUser.getAuthorities()
            );
            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        log.info("=== STOMP preSend END ===");

        return message;
    }
}