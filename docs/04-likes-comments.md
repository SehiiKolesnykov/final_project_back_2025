# Likes and Comments

## Likes

### POST /api/likes/{postId}

Перемкнути лайк на пості (потрібна автентифікація).

#### Path Parameters

| Параметр | Тип | Опис |
|----------|-----|------|
| postId | UUID | ID поста |

#### Request Body

| Поле | Тип | Обов'язкове | Опис |
|------|-----|-------------|------|
| userId* | UUID | Так | ID користувача |

#### Example Request

```json
{
  "userId": "123e4567-e89b-12d3-a456-426614174000"
}
```

#### Response

**Status**: `200 OK`

**Body**: `LikeDto`

```json
{
  "postId": "456e7890-e89b-12d3-a456-426614174001",
  "userId": "123e4567-e89b-12d3-a456-426614174000",
  "liked": true,
  "totalLikes": 10
}
```

#### LikeDto Structure

```typescript
{
  postId: UUID,      // ID поста
  userId: UUID,      // ID користувача
  liked: boolean,    // true - лайк додано, false - лайк видалено
  totalLikes: number // Загальна кількість лайків на пості
}
```

---

### GET /api/likes/{postId}/count

Отримати кількість лайків на пості.

#### Path Parameters

| Параметр | Тип | Опис |
|----------|-----|------|
| postId | UUID | ID поста |

#### Response

**Status**: `200 OK`

**Body**: Число (plain text)

```
10
```

---

## Comments

### POST /api/comments/{postId}

Додати коментар до поста (потрібна автентифікація).

#### Path Parameters

| Параметр | Тип | Опис |
|----------|-----|------|
| postId | UUID | ID поста |

#### Request Body

| Поле | Тип | Обов'язкове | Опис |
|------|-----|-------------|------|
| userId* | UUID | Так | ID користувача |
| content* | string | Так | Текст коментаря (максимум 500 символів) |

#### Example Request

```json
{
  "userId": "123e4567-e89b-12d3-a456-426614174000",
  "content": "Great post!"
}
```

#### Response

**Status**: `200 OK`

**Body**: `CommentDto`

```json
{
  "id": "789e0123-e89b-12d3-a456-426614174002",
  "content": "Great post!",
  "authorId": "123e4567-e89b-12d3-a456-426614174000",
  "postId": "456e7890-e89b-12d3-a456-426614174001",
  "createdAt": "2026-01-10T15:00:00"
}
```

---

### GET /api/comments/{postId}

Отримати коментарі до поста (сортування: найновіші першими).

#### Path Parameters

| Параметр | Тип | Опис |
|----------|-----|------|
| postId | UUID | ID поста |

#### Response

**Status**: `200 OK`

**Body**: Масив `CommentDto`

```json
[
  {
    "id": "789e0123-e89b-12d3-a456-426614174002",
    "content": "Great post!",
    "authorId": "123e4567-e89b-12d3-a456-426614174000",
    "postId": "456e7890-e89b-12d3-a456-426614174001",
    "createdAt": "2026-01-10T15:00:00"
  },
  {
    "id": "012e3456-e89b-12d3-a456-426614174003",
    "content": "Nice!",
    "authorId": "456e7890-e89b-12d3-a456-426614174001",
    "postId": "456e7890-e89b-12d3-a456-426614174001",
    "createdAt": "2026-01-10T15:05:00"
  }
]
```

---

## CommentDto Structure

```typescript
{
  id: UUID,          // Унікальний ідентифікатор коментаря
  content: string,   // Текст коментаря
  authorId: UUID,    // ID автора коментаря
  postId: UUID,      // ID поста
  createdAt: string  // ISO 8601 timestamp
}
```

---

## Примітки

### Лайки
- Лайк працює як перемикач (toggle): якщо лайк вже є - він видаляється, якщо немає - додається
- Поле `liked` у відповіді показує поточний стан після операції
- `totalLikes` показує загальну кількість лайків на пості після операції

### Коментарі
- Максимальна довжина коментаря: 500 символів
- Коментарі сортуються за датою створення (найновіші першими)
- Для отримання інформації про автора коментаря використовуйте `authorId` та ендпоінт `/api/user/{id}`

---

<div align="center">

**Document Information**  
Version 1.0.0 | Last Updated: January 11, 2026

</div>