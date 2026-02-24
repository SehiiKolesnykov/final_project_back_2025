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

        if (StompCommand.CONNECT.equals(accessor.getCommand()) ||
                StompCommand.SEND.equals(accessor.getCommand())) {

            String authHeader = accessor.getFirstNativeHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new IllegalStateException("Authorization header missing or invalid in WebSocket");
            }

            String token = authHeader.substring(7);
            try {
                String username = jwtService.extractUsername(token);
                User user = userRepository.findByEmail(username)
                        .orElseThrow(() -> new IllegalArgumentException("User not found for token"));

                if (!jwtService.validateToken(token, user)) {
                    throw new IllegalStateException("Invalid JWT token");
                }

                Authentication auth = new UsernamePasswordAuthenticationToken(
                        user, null, user.getAuthorities()
                );
                SecurityContextHolder.getContext().setAuthentication(auth);
                accessor.setUser(() -> user.getEmail());

            } catch (Exception e) {
                throw new IllegalStateException("WebSocket authentication failed: " + e.getMessage(), e);
            }
        }

        return message;
    }
}