# Chat

## Огляд

Chat API надає функціонал приватних повідомлень між користувачами. Підтримує як REST API для отримання історії повідомлень, так і WebSocket для обміну повідомленнями в реальному часі.

---

## Threads (Треди розмов)

### GET /api/chat/thread/{otherUserId}

Отримати або створити тред розмови з іншим користувачем (потрібна автентифікація).

#### Path Parameters

| Параметр | Тип | Опис |
|----------|-----|------|
| otherUserId | UUID | ID користувача, з яким починається розмова |

#### Response

**Status**: `200 OK`

**Body**: `ChatThreadResponse`

```json
{
  "id": 1,
  "participants": [
    {
      "id": "123e4567-e89b-12d3-a456-426614174000",
      "username": "john.doe",
      "email": "john.doe@example.com"
    },
    {
      "id": "456e7890-e89b-12d3-a456-426614174001",
      "username": "jane.smith",
      "email": "jane.smith@example.com"
    }
  ],
  "createdAt": "2026-01-10T14:00:00",
  "updatedAt": "2026-01-10T14:05:00",
  "unreadCount": 2,
  "otherParticipant": {
    "id": "456e7890-e89b-12d3-a456-426614174001",
    "username": "jane.smith",
    "email": "jane.smith@example.com"
  }
}
```

#### Примітки

- Якщо тред вже існує - повертає існуючий
- Якщо треда немає - автоматично створює новий
- `otherParticipant` - це другий учасник розмови (не поточний користувач)

---

### GET /api/chat/threads

Отримати всі треди розмов поточного користувача (потрібна автентифікація).

#### Response

**Status**: `200 OK`

**Body**: Масив `ChatThreadResponse`

```json
[
  {
    "id": 1,
    "participants": [
      {
        "id": "123e4567-e89b-12d3-a456-426614174000",
        "username": "john.doe",
        "email": "john.doe@example.com"
      },
      {
        "id": "456e7890-e89b-12d3-a456-426614174001",
        "username": "jane.smith",
        "email": "jane.smith@example.com"
      }
    ],
    "createdAt": "2026-01-10T14:00:00",
    "updatedAt": "2026-01-10T14:05:00",
    "unreadCount": 2,
    "otherParticipant": {
      "id": "456e7890-e89b-12d3-a456-426614174001",
      "username": "jane.smith",
      "email": "jane.smith@example.com"
    }
  },
  {
    "id": 2,
    "participants": [
      {
        "id": "123e4567-e89b-12d3-a456-426614174000",
        "username": "john.doe",
        "email": "john.doe@example.com"
      },
      {
        "id": "012e3456-e89b-12d3-a456-426614174003",
        "username": "bob.johnson",
        "email": "bob.johnson@example.com"
      }
    ],
    "createdAt": "2026-01-09T12:00:00",
    "updatedAt": "2026-01-09T12:05:00",
    "unreadCount": 0,
    "otherParticipant": {
      "id": "012e3456-e89b-12d3-a456-426614174003",
      "username": "bob.johnson",
      "email": "bob.johnson@example.com"
    }
  }
]
```

#### Примітки

- Треди сортуються за датою останнього оновлення (найновіші першими)
- `unreadCount` показує кількість непрочитаних повідомлень у треді

---

## Messages (Повідомлення)

### GET /api/chat/thread/{threadId}/messages

Отримати повідомлення в треді та позначити їх як прочитані (потрібна автентифікація).

#### Path Parameters

| Параметр | Тип | Опис |
|----------|-----|------|
| threadId | Long | ID треда |

#### Response

**Status**: `200 OK`

**Body**: Масив `ChatMessageResponse`

```json
[
  {
    "id": 1,
    "threadId": 1,
    "senderId": "123e4567-e89b-12d3-a456-426614174000",
    "senderUsername": "john.doe",
    "content": "Hello!",
    "createdAt": "2026-01-10T14:01:00",
    "messageType": "TEXT"
  },
  {
    "id": 2,
    "threadId": 1,
    "senderId": "456e7890-e89b-12d3-a456-426614174001",
    "senderUsername": "jane.smith",
    "content": "Hi there!",
    "createdAt": "2026-01-10T14:02:00",
    "messageType": "TEXT"
  }
]
```

#### Примітки

- Повідомлення сортуються за датою створення (від старіших до новіших)
- Після виклику цього ендпоінту всі повідомлення в треді позначаються як прочитані
- `unreadCount` треда автоматично оновлюється до 0

---

### POST /api/chat/thread/{threadId}/read

Позначити всі повідомлення в треді як прочитані (потрібна автентифікація).

#### Path Parameters

| Параметр | Тип | Опис |
|----------|-----|------|
| threadId | Long | ID треда |

#### Response

**Status**: `200 OK`

#### Примітки

- Використовується для позначення повідомлень як прочитаних без їх завантаження
- Зручно для оновлення статусу після отримання повідомлень через WebSocket

---

## Data Structures

### ChatThreadResponse

```typescript
{
  id: number,                    // Унікальний ідентифікатор треда
  participants: UserBasicInfo[], // Учасники розмови (завжди 2)
  createdAt: string,             // ISO 8601 timestamp створення
  updatedAt: string,             // ISO 8601 timestamp останнього повідомлення
  unreadCount: number,           // Кількість непрочитаних повідомлень
  otherParticipant: UserBasicInfo // Другий учасник (не поточний користувач)
}
```

### ChatMessageResponse

```typescript
{
  id: number,           // Унікальний ідентифікатор повідомлення
  threadId: number,     // ID треда
  senderId: UUID,       // ID відправника
  senderUsername: string, // Username відправника
  content: string,      // Текст повідомлення
  createdAt: string,    // ISO 8601 timestamp
  messageType: string   // Тип повідомлення (поки що тільки "TEXT")
}
```

### UserBasicInfo

```typescript
{
  id: UUID,           // Унікальний ідентифікатор користувача
  username: string,   // Username
  email: string       // Email
}
```

---

## Real-time Messaging

Для обміну повідомленнями в реальному часі використовуйте WebSocket з'єднання. Детальна інформація в розділі [WebSockets](09-websockets.md).

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

### Отримання повідомлень

**Subscription**: `/topic/chat/{threadId}`

**Message**: `ChatMessageResponse`

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

---

## Use Cases

### Початок нової розмови

1. Викликати `GET /api/chat/thread/{otherUserId}` для створення/отримання треда
2. Підписатися на WebSocket топік `/topic/chat/{threadId}`
3. Завантажити історію повідомлень через `GET /api/chat/thread/{threadId}/messages`
4. Надсилати нові повідомлення через WebSocket

### Відображення списку розмов

1. Викликати `GET /api/chat/threads` для отримання всіх тредів
2. Відобразити список з `otherParticipant` та `unreadCount`
3. Оновлювати `unreadCount` при отриманні нових повідомлень через WebSocket

### Позначення як прочитане

1. **Варіант А**: Викликати `GET /api/chat/thread/{threadId}/messages` - завантажить повідомлення і позначить як прочитані
2. **Варіант Б**: Викликати `POST /api/chat/thread/{threadId}/read` - тільки позначить як прочитані без завантаження

---

## Примітки

- Кожен тред має рівно двох учасників
- Повідомлення зберігаються постійно (не видаляються автоматично)
- Підтримується тільки текстовий тип повідомлень (`TEXT`)
- Для майбутніх версій заплановано підтримку файлів, зображень та інших типів
- WebSocket з'єднання необхідне для отримання повідомлень в реальному часі

---

<div align="center">

**Document Information**  
Version 1.0.0 | Last Updated: January 11, 2026

</div>