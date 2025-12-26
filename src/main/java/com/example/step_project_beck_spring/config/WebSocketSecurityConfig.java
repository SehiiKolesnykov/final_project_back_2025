//ЦЕЙ ФАЙЛ БУВ СТВОРЕНИЙ ДЛЯ ВЛАСНОГО ФРОНТА





package com.example.step_project_beck_spring.config;

import com.example.step_project_beck_spring.repository.UserRepository;
import com.example.step_project_beck_spring.service.JwtService;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.List;

@Configuration
@Order(Ordered.HIGHEST_PRECEDENCE + 99)
public class WebSocketSecurityConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    public WebSocketSecurityConfig(JwtService jwtService, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                
                if (accessor == null) {
                    return message;
                }
                
                StompCommand command = accessor.getCommand();
                

                if (command == StompCommand.CONNECT || 
                    command == StompCommand.SEND || 
                    command == StompCommand.SUBSCRIBE ||
                    command == StompCommand.UNSUBSCRIBE) {
                    
                    System.out.println("=== WebSocketSecurityConfig: Processing " + command + " ===");
                    

                    java.security.Principal principal = accessor.getUser();
                    System.out.println("Principal from session: " + (principal != null ? principal.getClass().getName() : "null"));
                    
                    final Authentication[] authToSet = {null};
                    

                    if (principal instanceof Authentication existingAuth) {
                        Object principalObj = existingAuth.getPrincipal();
                        if (principalObj instanceof com.example.step_project_beck_spring.entities.User user) {
                            System.out.println("Found User in existing Authentication: " + user.getEmail());
                            authToSet[0] = existingAuth;
                            accessor.setUser(existingAuth);
                        } else {
                            System.out.println("Principal is not User, trying token...");
                        }
                    } else if (principal instanceof com.example.step_project_beck_spring.entities.User user) {
                        System.out.println("Found User in Principal: " + user.getEmail());
                        authToSet[0] = new UsernamePasswordAuthenticationToken(
                                user,
                                null,
                                user.getAuthorities()
                        );
                        accessor.setUser(authToSet[0]);
                    }

                    if (authToSet[0] == null) {
                        List<String> authHeaders = accessor.getNativeHeader("Authorization");
                        final String token;
                        
                        if (authHeaders != null && !authHeaders.isEmpty()) {
                            String authHeader = authHeaders.get(0);
                            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                                token = authHeader.substring(7);
                                System.out.println("Token found in headers");
                            } else {
                                token = null;
                                System.out.println("No Bearer token in headers");
                            }
                        } else {
                            token = null;
                            System.out.println("No Authorization header found");
                        }

                        if (token != null) {
                            String userEmail = jwtService.extractUsername(token);
                            if (userEmail != null) {
                                System.out.println("Extracted email from token: " + userEmail);
                                final String finalToken = token;
                                userRepository.findByEmail(userEmail)
                                        .filter(user -> jwtService.validateToken(finalToken, user))
                                        .ifPresent(user -> {
                                            System.out.println("✅ Token validated for user: " + user.getEmail());
                                            Authentication auth = new UsernamePasswordAuthenticationToken(
                                                    user,
                                                    null,
                                                    user.getAuthorities()
                                            );
                                            accessor.setUser(auth);
                                            authToSet[0] = auth;
                                        });
                            } else {
                                System.out.println("NO Could not extract email from token");
                            }
                        } else {
                            System.out.println("NO No token available for authentication");
                        }
                    }
                    

                    if (authToSet[0] != null) {

                        accessor.setUser(authToSet[0]);
                        

                        org.springframework.security.core.context.SecurityContext securityContext = 
                            org.springframework.security.core.context.SecurityContextHolder.createEmptyContext();
                        securityContext.setAuthentication(authToSet[0]);
                        org.springframework.security.core.context.SecurityContextHolder.setContext(securityContext);
                        
                        System.out.println("OK Authentication set in SecurityContext for thread: " + Thread.currentThread().getName());
                        System.out.println("OK Authentication set in StompHeaderAccessor");
                        System.out.println("OK SecurityContext contains: " + (org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication() != null ? "Authentication" : "null"));
                    } else {
                        System.out.println("BAD No authentication available");
                    }
                }
                
                return message;
            }
        });
    }
}

