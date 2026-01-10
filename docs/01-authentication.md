# Authentication

## POST /api/auth/register

Реєстрація нового користувача. Надсилає email з кодом верифікації.

### Request Body

| Поле | Тип | Обов'язкове | Опис |
|------|-----|-------------|------|
| firstName* | string | Так | Ім'я |
| lastName* | string | Так | Прізвище |
| email* | string | Так | Email (валідний формат) |
| password* | string | Так | Пароль (мінімум 6 символів) |
| birthDate* | string | Так | Дата народження (формат: `dd.MM.yyyy`) |

### Example Request

```json
{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@example.com",
  "password": "securepassword",
  "birthDate": "01.01.1990"
}
```

### Response

**Status**: `200 OK`

**Body**: Пусте (код верифікації надіслано на email)

### Error Responses

**409 Conflict** - користувач з таким email вже існує:
```json
{
  "error": "User with this email already exists"
}
```

**400 Bad Request** - помилки валідації:
```json
{
  "password": "Password must be at least 6 characters"
}
```

---

## POST /api/auth/verify

Верифікація email за допомогою коду та автоматичний вхід (повертає JWT).

### Request Body

| Поле | Тип | Обов'язкове | Опис |
|------|-----|-------------|------|
| email* | string | Так | Email користувача |
| verificationCode* | string | Так | 6-значний код верифікації |

### Example Request

```json
{
  "email": "john.doe@example.com",
  "verificationCode": "123456"
}
```

### Response

**Status**: `200 OK`

**Body**:
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

### Error Responses

**400 Bad Request** - невалідний код:
```json
{
  "error": "Invalid verification code"
}
```

---

## POST /api/auth/login

Вхід в систему з email та паролем. Повертає JWT токен.

### Request Body

| Поле | Тип | Обов'язкове | Опис |
|------|-----|-------------|------|
| email* | string | Так | Email користувача |
| password* | string | Так | Пароль |
| rememberMe | boolean | Ні | `true` для токену на 7 днів (за замовчуванням: `false`) |

### Example Request

```json
{
  "email": "john.doe@example.com",
  "password": "securepassword",
  "rememberMe": true
}
```

### Response

**Status**: `200 OK`

**Body**:
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

### Error Responses

**401 Unauthorized** - невалідні дані:
```json
{
  "error": "Invalid email or password"
}
```

**400 Bad Request** - email не верифіковано:
```json
{
  "error": "Email not verified! Please check your email."
}
```

---

## GET /api/auth/test-login

**Тільки для розробки!** Автоматичний вхід як тестовий користувач з встановленням JWT у HttpOnly cookie.

### Request Body

Немає

### Response

**Status**: `200 OK`

**Body** (plain text):
```
Test user logged in. JWT cookie set.
```

**Cookie**:
```
jwt=<token>; HttpOnly; Secure=false; SameSite=None; Max-Age=604800
```

### Примітки

- Використовується тільки в режимі розробки
- Cookie діє 7 днів (604800 секунд)
- Встановлює JWT токен напряму в HttpOnly cookie

---

<div align="center">

**Document Information**  
Version 1.0.0 | Last Updated: January 11, 2026

</div>