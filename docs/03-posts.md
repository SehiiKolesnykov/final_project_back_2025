# Posts

## POST /api/posts

Створити новий пост (потрібна автентифікація).

### Request Body

| Поле | Тип | Обов'язкове | Опис |
|------|-----|-------------|------|
| content* | string | Так | Текст поста (максимум 280 символів) |
| imageUrl | string | Ні | URL зображення |

### Example Request

```json
{
  "content": "Hello world!",
  "imageUrl": "https://example.com/image.jpg"
}
```

### Response

**Status**: `200 OK`

**Body**: `PostDto`

```json
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "content": "Hello world!",
  "imageUrl": "https://example.com/image.jpg",
  "author": {
    "id": "456e7890-e89b-12d3-a456-426614174001",
    "firstName": "John",
    "lastName": "Doe",
    "avatarUrl": "https://example.com/avatar.jpg"
  },
  "createdAt": "2026-01-10T14:48:00",
  "likesCount": 0,
  "commentsCount": 0,
  "repostsCount": 0,
  "quotesCount": 0
}
```

---

## PATCH /api/posts/{id}

Оновити пост (потрібна автентифікація, тільки власник).

### Path Parameters

| Параметр | Тип | Опис |
|----------|-----|------|
| id | UUID | ID поста |

### Request Body

| Поле | Тип | Обов'язкове | Опис |
|------|-----|-------------|------|
| content | string | Ні | Текст поста (максимум 280 символів) |
| imageUrl | string | Ні | URL зображення |

### Example Request

```json
{
  "content": "Updated hello!",
  "imageUrl": "https://new-image.com/image.jpg"
}
```

### Response

**Status**: `200 OK`

**Body**: `PostDto`

### Error Responses

**403 Forbidden**:
```json
{
  "error": "Only owner can update post"
}
```

**404 Not Found**:
```json
{
  "error": "Post not found"
}
```

---

## DELETE /api/posts/{id}

Видалити пост (потрібна автентифікація, тільки власник).

### Path Parameters

| Параметр | Тип | Опис |
|----------|-----|------|
| id | UUID | ID поста |

### Response

**Status**: `204 No Content`

### Error Responses

**403 Forbidden**:
```json
{
  "error": "Only owner can delete post or post not found"
}
```

---

## GET /api/posts/feed?page={page}&size={size}

Отримати стрічку постів від користувачів, на яких підписаний (потрібна автентифікація).

### Query Parameters

| Параметр | Тип | За замовчуванням | Опис |
|----------|-----|------------------|------|
| page | number | 0 | Номер сторінки |
| size | number | 20 | Розмір сторінки |

### Response

**Status**: `200 OK`

**Body**: Масив `PostDto`

```json
[
  {
    "id": "123e4567-e89b-12d3-a456-426614174000",
    "content": "Post from followed user",
    "imageUrl": null,
    "author": {
      "id": "456e7890-e89b-12d3-a456-426614174001",
      "firstName": "Jane",
      "lastName": "Smith",
      "avatarUrl": "https://example.com/jane.jpg"
    },
    "createdAt": "2026-01-10T14:00:00",
    "likesCount": 5,
    "commentsCount": 2,
    "repostsCount": 1,
    "quotesCount": 0
  }
]
```

---

## GET /api/posts/recommended?page={page}&size={size}

Отримати рекомендовані пости (популярні, від користувачів на яких не підписаний; потрібна автентифікація).

### Query Parameters

| Параметр | Тип | За замовчуванням | Опис |
|----------|-----|------------------|------|
| page | number | 0 | Номер сторінки |
| size | number | 20 | Розмір сторінки |

### Response

**Status**: `200 OK`

**Body**: Масив `PostDto`

---

## GET /api/posts/{id}

Отримати пост за ID.

### Path Parameters

| Параметр | Тип | Опис |
|----------|-----|------|
| id | UUID | ID поста |

### Response

**Status**: `200 OK`

**Body**: Один об'єкт `PostDto`

```json
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "content": "Single post content",
  "imageUrl": "https://example.com/image.jpg",
  "author": {
    "id": "456e7890-e89b-12d3-a456-426614174001",
    "firstName": "John",
    "lastName": "Doe",
    "avatarUrl": "https://example.com/avatar.jpg"
  },
  "createdAt": "2026-01-10T14:48:00",
  "likesCount": 15,
  "commentsCount": 5,
  "repostsCount": 2,
  "quotesCount": 1
}
```

### Error Responses

**404 Not Found**:
```json
{
  "error": "Post not found"
}
```

---

## GET /api/posts/user/{userId}?page={page}&size={size}

Отримати пости конкретного користувача (публічний ендпоінт).

### Path Parameters

| Параметр | Тип | Опис |
|----------|-----|------|
| userId | UUID | ID користувача |

### Query Parameters

| Параметр | Тип | За замовчуванням | Опис |
|----------|-----|------------------|------|
| page | number | 0 | Номер сторінки |
| size | number | 20 | Розмір сторінки |

### Response

**Status**: `200 OK`

**Body**: Масив `PostDto`

---

## POST /api/posts/{id}/save

Зберегти пост у вибране (потрібна автентифікація).

### Path Parameters

| Параметр | Тип | Опис |
|----------|-----|------|
| id | UUID | ID поста |

### Response

**Status**: `200 OK`

---

## DELETE /api/posts/{id}/save

Видалити пост з вибраного (потрібна автентифікація).

### Path Parameters

| Параметр | Тип | Опис |
|----------|-----|------|
| id | UUID | ID поста |

### Response

**Status**: `204 No Content`

---

## GET /api/posts/saved?page={page}&size={size}

Отримати збережені пости (потрібна автентифікація).

### Query Parameters

| Параметр | Тип | За замовчуванням | Опис |
|----------|-----|------------------|------|
| page | number | 0 | Номер сторінки |
| size | number | 20 | Розмір сторінки |

### Response

**Status**: `200 OK`

**Body**: Масив `PostDto`

---

## PostDto Structure

```typescript
{
  id: UUID,                // Унікальний ідентифікатор
  content: string,         // Текст поста
  imageUrl: string | null, // URL зображення
  author: {
    id: UUID,
    firstName: string,
    lastName: string,
    avatarUrl: string
  },
  createdAt: string,       // ISO 8601 timestamp
  likesCount: number,      // Кількість лайків
  commentsCount: number,   // Кількість коментарів
  repostsCount: number,    // Кількість репостів
  quotesCount: number      // Кількість цитувань
}
```

---

<div align="center">

**Document Information**  
Version 1.0.0 | Last Updated: January 11, 2026

</div>