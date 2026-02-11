// src/main/java/com/example/step_project_beck_spring/config/WebSocketConfig.java
package com.example.step_project_beck_spring.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

//Конфігурація WebSocket для пуш-нотифікацій.
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    //Реєструємо STOMP endpoint, до якого буде підключатися фронтенд.
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")           // Напр. ws://host/ws
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    //Налаштовуємо брокер повідомлень.
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic"); // Куди ми будемо слати повідомлення
        config.setApplicationDestinationPrefixes("/app"); // Префікс для повідомлень з клієнта (якщо треба)
        config.setUserDestinationPrefix("/user"); // Для user-specific каналів
    }
}