# Follows (Підписки)

## POST /api/follow/{targetUserId}

Підписатися на користувача (потрібна автентифікація).

### Path Parameters

| Параметр | Тип | Опис |
|----------|-----|------|
| targetUserId | UUID | ID користувача, на якого підписуєтесь |

### Response

**Status**: `200 OK`

---

## DELETE /api/follow/{targetUserId}

Відписатися від користувача (потрібна автентифікація).

### Path Parameters

| Параметр | Тип | Опис |
|----------|-----|------|
| targetUserId | UUID | ID користувача, від якого відписуєтесь |

### Response

**Status**: `204 No Content`

---

## GET /api/follow/status/{targetUserId}

Перевірити, чи підписаний на користувача (потрібна автентифікація).

### Path Parameters

| Параметр | Тип | Опис |
|----------|-----|------|
| targetUserId | UUID | ID користувача для перевірки |

### Response

**Status**: `200 OK`

**Body**: Boolean (plain text)

```
true
```

або

```
false
```

---

## GET /api/follow/me/following

Отримати список користувачів, на яких я підписаний (потрібна автентифікація).

### Response

**Status**: `200 OK`

**Body**: Масив `UserSummaryDto`

```json
[
  {
    "id": "123e4567-e89b-12d3-a456-426614174000",
    "firstName": "Jane",
    "lastName": "Smith",
    "avatarUrl": "https://example.com/jane.jpg"
  },
  {
    "id": "456e7890-e89b-12d3-a456-426614174001",
    "firstName": "Bob",
    "lastName": "Johnson",
    "avatarUrl": "https://example.com/bob.jpg"
  }
]
```

---

## GET /api/follow/me/followers

Отримати список моїх підписників (потрібна автентифікація).

### Response

**Status**: `200 OK`

**Body**: Масив `UserSummaryDto`

```json
[
  {
    "id": "123e4567-e89b-12d3-a456-426614174000",
    "firstName": "Alice",
    "lastName": "Wonder",
    "avatarUrl": "https://example.com/alice.jpg"
  },
  {
    "id": "456e7890-e89b-12d3-a456-426614174001",
    "firstName": "Eve",
    "lastName": "Adams",
    "avatarUrl": "https://example.com/eve.jpg"
  }
]
```

---

## GET /api/follow/{userId}/following

Отримати список користувачів, на яких підписаний конкретний користувач (публічний ендпоінт).

### Path Parameters

| Параметр | Тип | Опис |
|----------|-----|------|
| userId | UUID | ID користувача |

### Response

**Status**: `200 OK`

**Body**: Масив `UserSummaryDto`

```json
[
  {
    "id": "123e4567-e89b-12d3-a456-426614174000",
    "firstName": "Jane",
    "lastName": "Smith",
    "avatarUrl": "https://example.com/jane.jpg"
  }
]
```

---

## GET /api/follow/{userId}/followers

Отримати список підписників конкретного користувача (публічний ендпоінт).

### Path Parameters

| Параметр | Тип | Опис |
|----------|-----|------|
| userId | UUID | ID користувача |

### Response

**Status**: `200 OK`

**Body**: Масив `UserSummaryDto`

```json
[
  {
    "id": "123e4567-e89b-12d3-a456-426614174000",
    "firstName": "Alice",
    "lastName": "Wonder",
    "avatarUrl": "https://example.com/alice.jpg"
  }
]
```

---

## GET /api/follow/{userId}/followers/count

Отримати кількість підписників користувача (публічний ендпоінт).

### Path Parameters

| Параметр | Тип | Опис |
|----------|-----|------|
| userId | UUID | ID користувача |

### Response

**Status**: `200 OK`

**Body**: Число (plain text)

```
100
```

---

## GET /api/follow/{userId}/following/count

Отримати кількість підписок користувача (публічний ендпоінт).

### Path Parameters

| Параметр | Тип | Опис |
|----------|-----|------|
| userId | UUID | ID користувача |

### Response

**Status**: `200 OK`

**Body**: Число (plain text)

```
50
```

---

## UserSummaryDto Structure

```typescript
{
  id: UUID,          // Унікальний ідентифікатор
  firstName: string, // Ім'я
  lastName: string,  // Прізвище
  avatarUrl: string  // URL аватара
}
```

---

## Примітки

- Неможливо підписатися на самого себе
- Повторна підписка на користувача не призводить до помилки
- Відписка від користувача, на якого не підписаний, також не призводить до помилки
- Лічильники підписників/підписок оновлюються автоматично при підписці/відписці
- Ендпоінти `/me/following` та `/me/followers` повертають актуальну інформацію про підписки поточного користувача

---

<div align="center">

**Document Information**  
Version 1.0.0 | Last Updated: January 11, 2026

</div>