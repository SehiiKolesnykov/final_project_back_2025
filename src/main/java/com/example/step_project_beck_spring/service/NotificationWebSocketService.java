package com.example.step_project_beck_spring.service;

import com.example.step_project_beck_spring.dto.NotificationDto;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import java.util.UUID;

//Сервіс, який відповідає за відправку WebSocket-повідомлень конкретному користувачу.
@Service
@RequiredArgsConstructor
public class NotificationWebSocketService {
    private final SimpMessagingTemplate messagingTemplate;

    //Відправити нову нотифікацію конкретному користувачу.
    //Канал: /topic/notifications/{userId}
    public void sendNotificationToUser(UUID userId, NotificationDto notificationDto) {
        String destination = "/topic/notifications/" + userId;
        messagingTemplate.convertAndSend(destination, notificationDto);
    }
}


