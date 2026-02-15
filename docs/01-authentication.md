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

## GET /oauth2/authorization/google

Ініціювати авторизацію через Google (OAuth 2.0 Login).
Перенаправляє користувача на сторінку Google для входу (автентифікація не потрібна, це публічний ендпоінт).

**Опис**

Користувач натискає кнопку «Увійти через Google» на фронтенді.
Бекенд перенаправляє на Google OAuth сторінку.
Після згоди Google повертає код авторизації на бекенд (/login/oauth2/code/google).
Бекенд обмінює код на токени, знаходить/створює користувача за email або googleId, видає JWT у HttpOnly cookie та перенаправляє на фронт.

**Response**
```Status: 302 Found — редирект на сторінку Google```
```Location: https://accounts.google.com/o/oauth2/v2/auth?... (з параметрами client_id, redirect_uri тощо)```

**Після успішного входу**

Google редиректить назад на бекенд:
https://step-project-api.onrender.com/login/oauth2/code/google?...
Бекенд обробляє код → видає JWT-куку → редиректить на фронт (наприклад https://widi-rho.vercel.app/auth)

Автоматична реєстрація: якщо користувача за email або googleId не знайдено — створюється новий запис у базі.

Поле googleId (sub з Google) зберігається в сутності User для швидкого пошуку при наступних входах.

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