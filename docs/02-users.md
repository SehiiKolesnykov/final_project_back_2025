# Users

## GET /api/user

Отримати список всіх користувачів (публічний ендпоінт).

### Request Body

Немає

### Response

**Status**: `200 OK`

**Body**: Масив `UserPublicDTO`

```json
[
  {
    "id": "123e4567-e89b-12d3-a456-426614174000",
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@example.com",
    "avatarUrl": "https://example.com/avatar.jpg",
    "backgroundImg": "https://example.com/background.jpg",
    "followingCount": 50,
    "followersCount": 100,
    "postsCount": 20,
    "isFollowing": false
  }
]
```

---

## GET /api/user/search?q={query}

Пошук користувачів за ім'ям або email (без урахування регістру, виключає поточного користувача).

### Query Parameters

| Параметр | Тип | Обов'язковий | Опис |
|----------|-----|--------------|------|
| q | string | Так | Пошуковий запит |

### Example Request

```
GET /api/user/search?q=john
```

### Response

**Status**: `200 OK`

**Body**: Масив `UserPublicDTO`

```json
[
  {
    "id": "123e4567-e89b-12d3-a456-426614174000",
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@example.com",
    "avatarUrl": "https://example.com/avatar.jpg",
    "backgroundImg": "https://example.com/background.jpg",
    "followingCount": 50,
    "followersCount": 100,
    "postsCount": 20,
    "isFollowing": false
  }
]
```

### Error Responses

**400 Bad Request** - пустий пошуковий запит

---

## GET /api/user/{id}

Отримати публічний профіль користувача за ID.

### Path Parameters

| Параметр | Тип | Опис |
|----------|-----|------|
| id | UUID | ID користувача |

### Response

**Status**: `200 OK`

**Body**: Один об'єкт `UserPublicDTO`

```json
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@example.com",
  "avatarUrl": "https://example.com/avatar.jpg",
  "backgroundImg": "https://example.com/background.jpg",
  "followingCount": 50,
  "followersCount": 100,
  "postsCount": 20,
  "isFollowing": false
}
```

### Error Responses

**404 Not Found** - користувача не знайдено

---

## GET /api/user/me

Отримати профіль поточного користувача (потрібна автентифікація).

### Request Body

Немає

### Response

**Status**: `200 OK`

**Body**: Один об'єкт `UserPublicDTO`

```json
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@example.com",
  "avatarUrl": "https://example.com/avatar.jpg",
  "backgroundImg": "https://example.com/background.jpg",
  "followingCount": 50,
  "followersCount": 100,
  "postsCount": 20,
  "isFollowing": false
}
```

---

## PATCH /api/user/update

Оновити профіль поточного користувача (потрібна автентифікація).

### Request Body

| Поле | Тип | Обов'язкове | Опис |
|------|-----|-------------|------|
| firstName | string | Ні | Ім'я |
| lastName | string | Ні | Прізвище |
| avatarUrl | string | Ні | URL аватара |
| backgroundImgUrl | string | Ні | URL фонового зображення |
| birthDate | LocalDate | Ні | Дата народження (формат: `yyyy-MM-dd`) |

### Example Request

```json
{
  "firstName": "Updated John",
  "lastName": "Updated Doe",
  "avatarUrl": "https://new-avatar.com/image.jpg"
}
```

### Response

**Status**: `200 OK`

**Body** (plain text):
```
Profile updated successfully
```

### Примітки

- Всі поля опціональні
- Оновлюються тільки ті поля, які передані в запиті
- Для завантаження зображень використовуйте ендпоінти `/api/upload/*`

---

## UserPublicDTO Structure

```typescript
{
  id: UUID,              // Унікальний ідентифікатор
  firstName: string,     // Ім'я
  lastName: string,      // Прізвище
  email: string,         // Email
  avatarUrl: string,     // URL аватара
  backgroundImg: string, // URL фонового зображення
  followingCount: number,// Кількість підписок
  followersCount: number,// Кількість підписників
  postsCount: number,    // Кількість постів
  isFollowing: boolean   // Чи підписаний поточний користувач
}
```

---

<div align="center">

**Document Information**  
Version 1.0.0 | Last Updated: January 11, 2026

</div>