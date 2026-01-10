# WebSockets

## Огляд

Ми використовуємо **STOMP over WebSocket** для комунікації в реальному часі. WebSocket з'єднання підтримує:
- Миттєве відправлення та отримання повідомлень у чаті
- Отримання сповіщень в реальному часі

---

## Connection (З'єднання)

### Endpoint

```
ws://localhost:9000/ws
```

**Примітка**: Endpoint підтримує SockJS fallback для браузерів без нативної підтримки WebSocket.

### Authentication

При встановленні з'єднання передайте JWT токен у заголовку:

```
Authorization: Bearer <token>
```

### Connection Example (JavaScript)

```javascript
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';

const socket = new SockJS('http://localhost:9000/ws');
const stompClient = new Client({
  webSocketFactory: () => socket,
  connectHeaders: {
    Authorization: `Bearer ${jwtToken}`
  },
  onConnect: (frame) => {
    console.log('Connected:', frame);
    // Підписатися на топіки
  },
  onStompError: (frame) => {
    console.error('STOMP error:', frame);
  }
});

stompClient.activate();
```

---

## Chat Messages (Повідомлення чату)

### Надсилання повідомлення

**Destination**: `/app/chat/send`

**Payload**: `ChatMessageRequest`

```json
{
  "threadId": 1,
  "recipientUserId": "456e7890-e89b-12d3-a456-426614174001",
  "content": "Hello!"
}
```

#### Example Code

```javascript
stompClient.publish({
  destination: '/app/chat/send',
  body: JSON.stringify({
    threadId: 1,
    recipientUserId: '456e7890-e89b-12d3-a456-426614174001',
    content: 'Hello!'
  })
});
```

---

### Отримання повідомлень

**Subscription**: `/topic/chat/{threadId}`

**Message Format**: `ChatMessageResponse`

```json
{
  "id": 3,
  "threadId": 1,
  "senderId": "123e4567-e89b-12d3-a456-426614174000",
  "senderUsername": "john.doe",
  "content": "Hello!",
  "createdAt": "2026-01-10T14:03:00",
  "messageType": "TEXT"
}
```

#### Example Code

```javascript
const subscription = stompClient.subscribe(
  '/topic/chat/1',
  (message) => {
    const chatMessage = JSON.parse(message.body);
    console.log('Received message:', chatMessage);
    // Оновити UI
  }
);
```

---

## Notifications (Сповіщення)

### Отримання сповіщень

**Subscription**: `/topic/notifications/{userId}`

**Message Format**: `NotificationDto`

```json
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "type": "NEW_POST",
  "message": "New post from John",
  "link": "/posts/456",
  "isRead": false,
  "createdAt": "2026-01-10T14:00:00"
}
```

#### Example Code

```javascript
const userId = '123e4567-e89b-12d3-a456-426614174000';

const subscription = stompClient.subscribe(
  `/topic/notifications/${userId}`,
  (message) => {
    const notification = JSON.parse(message.body);
    console.log('Received notification:', notification);
    // Показати сповіщення в UI
  }
);
```

---

## Complete Example

### React + TypeScript Implementation

```typescript
import { useEffect, useState } from 'react';
import SockJS from 'sockjs-client';
import { Client, IMessage } from '@stomp/stompjs';

interface ChatMessage {
  id: number;
  threadId: number;
  senderId: string;
  senderUsername: string;
  content: string;
  createdAt: string;
  messageType: string;
}

function useWebSocket(jwtToken: string, userId: string) {
  const [stompClient, setStompClient] = useState<Client | null>(null);
  const [connected, setConnected] = useState(false);

  useEffect(() => {
    const socket = new SockJS('http://localhost:9000/ws');
    const client = new Client({
      webSocketFactory: () => socket,
      connectHeaders: {
        Authorization: `Bearer ${jwtToken}`
      },
      onConnect: () => {
        console.log('WebSocket connected');
        setConnected(true);
        
        // Підписка на сповіщення
        client.subscribe(`/topic/notifications/${userId}`, (message) => {
          const notification = JSON.parse(message.body);
          console.log('New notification:', notification);
          // Обробка сповіщення
        });
      },
      onStompError: (frame) => {
        console.error('STOMP error:', frame);
        setConnected(false);
      },
      onWebSocketClose: () => {
        console.log('WebSocket closed');
        setConnected(false);
      }
    });

    client.activate();
    setStompClient(client);

    return () => {
      client.deactivate();
    };
  }, [jwtToken, userId]);

  const subscribeToChatThread = (
    threadId: number,
    onMessage: (message: ChatMessage) => void
  ) => {
    if (!stompClient || !connected) return null;

    return stompClient.subscribe(
      `/topic/chat/${threadId}`,
      (message: IMessage) => {
        const chatMessage = JSON.parse(message.body);
        onMessage(chatMessage);
      }
    );
  };

  const sendChatMessage = (
    threadId: number,
    recipientUserId: string,
    content: string
  ) => {
    if (!stompClient || !connected) {
      console.error('WebSocket not connected');
      return;
    }

    stompClient.publish({
      destination: '/app/chat/send',
      body: JSON.stringify({
        threadId,
        recipientUserId,
        content
      })
    });
  };

  return {
    connected,
    subscribeToChatThread,
    sendChatMessage
  };
}

export default useWebSocket;
```

### Usage Example

```typescript
function ChatComponent() {
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const { connected, subscribeToChatThread, sendChatMessage } = useWebSocket(
    jwtToken,
    currentUserId
  );

  useEffect(() => {
    if (!connected) return;

    const subscription = subscribeToChatThread(threadId, (message) => {
      setMessages(prev => [...prev, message]);
    });

    return () => {
      subscription?.unsubscribe();
    };
  }, [connected, threadId]);

  const handleSendMessage = (content: string) => {
    sendChatMessage(threadId, recipientUserId, content);
  };

  return (
    // UI компонент
  );
}
```

---

## Best Practices

### 1. Connection Management

- Встановлюйте з'єднання один раз при завантаженні додатку
- Закривайте з'єднання при виході користувача
- Обробляйте переривання з'єднання та автоматичне перепідключення

```javascript
const client = new Client({
  reconnectDelay: 5000, // Перепідключення через 5 секунд
  heartbeatIncoming: 4000,
  heartbeatOutgoing: 4000
});
```

### 2. Subscription Management

- Відписуйтесь від топіків при розмонтуванні компонентів
- Не створюйте дублікатні підписки

```javascript
useEffect(() => {
  const subscription = stompClient.subscribe(...);
  
  return () => {
    subscription?.unsubscribe();
  };
}, [dependencies]);
```

### 3. Error Handling

- Обробляйте помилки з'єднання
- Показуйте користувачу статус підключення
- Зберігайте повідомлення локально, якщо з'єднання розірвано

```javascript
const [connectionStatus, setConnectionStatus] = useState<
  'connected' | 'disconnected' | 'reconnecting'
>('disconnected');

const client = new Client({
  onConnect: () => setConnectionStatus('connected'),
  onStompError: () => setConnectionStatus('disconnected'),
  onWebSocketClose: () => setConnectionStatus('reconnecting')
});
```

### 4. Message Validation

- Завжди перевіряйте формат отриманих повідомлень
- Обробляйте невалідні дані

```javascript
stompClient.subscribe('/topic/chat/1', (message) => {
  try {
    const data = JSON.parse(message.body);
    if (validateChatMessage(data)) {
      handleMessage(data);
    }
  } catch (error) {
    console.error('Invalid message format:', error);
  }
});
```

---

## Troubleshooting

### Помилка: "Connection failed"

- Перевірте, що backend запущений на правильному порті
- Переконайтесь, що JWT токен валідний
- Перевірте CORS налаштування

### Помилка: "Unauthorized"

- JWT токен відсутній або невалідний
- Переконайтесь, що токен передається в заголовку `Authorization`

### Повідомлення не доставляються

- Перевірте, що підписка створена до надсилання повідомлення
- Переконайтесь, що використовується правильний `threadId`
- Перевірте логи backend на наявність помилок

### SockJS Fallback

Якщо браузер не підтримує WebSocket, SockJS автоматично використовує fallback:
- Long polling
- Server-Sent Events (SSE)
- Інші механізми транспорту

---

## Security Considerations

- JWT токен передається тільки при встановленні з'єднання
- Кожне повідомлення валідується на backend
- Користувачі можуть підписуватись тільки на свої топіки
- Backend перевіряє права доступу перед надсиланням повідомлень

---

## Performance Tips

- Використовуйте heartbeat для підтримки з'єднання
- Обмежуйте кількість одночасних підписок
- Відписуйтесь від неактивних тредів
- Оптимізуйте розмір повідомлень (уникайте великих payload)

---

<div align="center">

**Document Information**  
Version 1.0.0 | Last Updated: January 11, 2026

</div>