# Notifications

## GET /api/notifications?page={page}&size={size}

Отримати мої сповіщення (потрібна автентифікація).

### Query Parameters

| Параметр | Тип | За замовчуванням | Опис |
|----------|-----|------------------|------|
| page | number | 0 | Номер сторінки |
| size | number | 20 | Розмір сторінки |

### Response

**Status**: `200 OK`

**Body**: `NotificationPageDto`

```json
{
  "notifications": [
    {
      "id": "123e4567-e89b-12d3-a456-426614174000",
      "type": "NEW_POST",
      "message": "John Doe published a new post",
      "link": "/posts/456e7890-e89b-12d3-a456-426614174001",
      "isRead": false,
      "createdAt": "2026-01-10T14:00:00"
    },
    {
      "id": "789e0123-e89b-12d3-a456-426614174002",
      "type": "LIKE",
      "message": "Jane liked your post",
      "link": "/posts/012e3456-e89b-12d3-a456-426614174003",
      "isRead": false,
      "createdAt": "2026-01-10T14:05:00"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 2,
  "totalPages": 1,
  "hasNext": false
}
```

---

## POST /api/notifications/{notificationId}/read

Позначити сповіщення як прочитане (потрібна автентифікація).

### Path Parameters

| Параметр | Тип | Опис |
|----------|-----|------|
| notificationId | UUID | ID сповіщення |

### Response

**Status**: `200 OK`

---

## POST /api/notifications/read-all

Позначити всі сповіщення як прочитані (потрібна автентифікація).

### Response

**Status**: `200 OK`

---

## GET /api/notifications/unread-count

Отримати кількість непрочитаних сповіщень (потрібна автентифікація).

### Response

**Status**: `200 OK`

**Body**: Число (plain text)

```
5
```

---

## DELETE /api/notifications/{notificationId}

Видалити сповіщення

### Response

**Status**: `204 No Content — сповіщення успішно видалено`

---

## Notification Subscriptions

### POST /api/notifications/subscriptions/{targetUserId}

Підписатися на сповіщення від користувача (потрібна автентифікація).

#### Path Parameters

| Параметр | Тип | Опис |
|----------|-----|------|
| targetUserId | UUID | ID користувача |

#### Response

**Status**: `200 OK`

---

### DELETE /api/notifications/subscriptions/{targetUserId}

Відписатися від сповіщень користувача (потрібна автентифікація).

#### Path Parameters

| Параметр | Тип | Опис |
|----------|-----|------|
| targetUserId | UUID | ID користувача |

#### Response

**Status**: `204 No Content`

---

## Data Structures

### NotificationDto

```typescript
{
  id: UUID,          // Унікальний ідентифікатор
  type: string,      // Тип сповіщення (NEW_POST, LIKE, COMMENT, FOLLOW, тощо)
  message: string,   // Текст сповіщення
  link: string,      // Посилання на пов'язаний ресурс
  isRead: boolean,   // Чи прочитане сповіщення
  createdAt: string  // ISO 8601 timestamp
}
```

### NotificationPageDto

```typescript
{
  notifications: NotificationDto[], // Масив сповіщень
  page: number,                     // Поточна сторінка
  size: number,                     // Розмір сторінки
  totalElements: number,            // Загальна кількість елементів
  totalPages: number,               // Загальна кількість сторінок
  hasNext: boolean                  // Чи є наступна сторінка
}
```

---

## Notification Types

Можливі типи сповіщень:

| Тип | Опис |
|-----|------|
| `NEW_POST` | Новий пост від користувача, на якого підписані |
| `LIKE` | Хтось лайкнув ваш пост |
| `COMMENT` | Хтось прокоментував ваш пост |
| `FOLLOW` | Хтось підписався на вас |
| `REPOST` | Хтось зробив репост вашого поста |
| `QUOTE` | Хтось процитував ваш пост |
| `MENTION` | Хтось згадав вас в пості |

---

## Real-time Notifications

Для отримання сповіщень в реальному часі використовуйте WebSocket з'єднання. Див. розділ [WebSockets](09-websockets.md) для деталей.

**Підписка**: `/topic/notifications/{userId}`

**Формат повідомлення**: `NotificationDto` (JSON)

---

## Примітки

- Сповіщення сортуються за датою створення (найновіші першими)
- Прочитані сповіщення не видаляються автоматично
- Підписка на сповіщення окремого користувача незалежна від підписки на його профіль
- Непрочитані сповіщення можна використовувати для відображення бейджа в UI
- При отриманні сповіщень через WebSocket вони автоматично додаються до списку без необхідності оновлювати сторінку

---

<div align="center">

**Document Information**  
Version 1.0.0 | Last Updated: January 11, 2026

</div>