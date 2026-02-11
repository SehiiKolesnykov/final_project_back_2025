# Authentication

## POST /api/auth/register

Реєстрація нового користувача через Firebase Authentication (email + password).  
Після успішної реєстрації користувач одразу вважається верифікованим і отримує 
JWT у HttpOnly cookie.

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

**Body**: ```{
"token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}```
**Cookie**:
```
jwt=<token>
HttpOnly: true
Secure: true
SameSite: Lax
Path: /
Max-Age: 21600 (6 годин, якщо rememberMe не використовується при реєстрації)
```
### Error Responses

**409 Conflict** - користувач з таким email вже існує:
```json
{
  "error": "Email already taken / Цей email вже зайнятий"
}
```

**400 Bad Request** - помилки валідації:
```json
{
  "password": "Password must be at least 6 characters"
}
```

---

## POST /api/auth/login

Вхід через Firebase Authentication (email + password).
Повертає JWT у HttpOnly cookie.

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

### Примітки
- Cookie діє 7 днів (604800 секунд)
- Встановлює JWT токен напряму в HttpOnly cookie

---

<div align="center">

**Document Information**  
Version 1.0.0 | Last Updated: January 11, 2026

</div>