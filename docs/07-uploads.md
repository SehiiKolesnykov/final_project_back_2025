# Uploads (Завантаження файлів)

## Огляд

Beck Spring використовує **Cloudinary** для зберігання зображень. Файли завантажуються **напряму з frontend до Cloudinary** для забезпечення безпеки та продуктивності.

> ⚠️ **Важливо**: Ніколи не надсилайте файли через backend API!

---

## Workflow (Послідовність дій)

### 1. Запит підпису

Frontend запитує підписані параметри у backend:

```
GET /api/upload/signature/{type}
```

Доступні типи:
- `avatar` - для аватарів користувачів
- `background` - для фонових зображень
- `post` - для зображень у постах

### 2. Отримання параметрів

Backend повертає підписані параметри для авторизації завантаження:

```json
{
  "cloudName": "your-cloud-name",
  "apiKey": "1234567890",
  "signature": "abc123def456",
  "timestamp": 1704892080,
  "folder": "users/avatars/123e4567-e89b-12d3-a456-426614174000"
}
```

### 3. Завантаження файлу

Frontend використовує Cloudinary SDK або widget для прямого завантаження:

```javascript
// Приклад з Cloudinary SDK
cloudinary.uploader.upload(file, {
  cloudName: params.cloudName,
  apiKey: params.apiKey,
  signature: params.signature,
  timestamp: params.timestamp,
  folder: params.folder
})
```

### 4. Отримання URL

Cloudinary повертає `public_id` та `secure_url`:

```json
{
  "public_id": "users/avatars/123e4567-e89b-12d3-a456-426614174000/image123",
  "secure_url": "https://res.cloudinary.com/your-cloud/image/upload/v1234567890/users/avatars/123e4567-e89b-12d3-a456-426614174000/image123.jpg"
}
```

### 5. Збереження URL

Frontend зберігає `secure_url` в сутності користувача/поста через відповідні API:

- Для аватара: `PATCH /api/user/update` з полем `avatarUrl`
- Для фону: `PATCH /api/user/update` з полем `backgroundImgUrl`
- Для поста: `POST /api/posts` або `PATCH /api/posts/{id}` з полем `imageUrl`

---

## API Endpoints

### GET /api/upload/signature/{type}

Отримати підписані параметри для прямого завантаження (потрібна автентифікація).

#### Path Parameters

| Параметр | Тип | Опис |
|----------|-----|------|
| type | string | Тип завантаження: `avatar`, `background`, або `post` |

#### Example Request

```
GET /api/upload/signature/avatar
```

#### Response

**Status**: `200 OK`

**Body**:

```json
{
  "cloudName": "your-cloud-name",
  "apiKey": "1234567890",
  "signature": "abc123def456",
  "timestamp": 1704892080,
  "folder": "users/avatars/123e4567-e89b-12d3-a456-426614174000"
}
```

#### Response Fields

| Поле | Тип | Опис |
|------|-----|------|
| cloudName | string | Назва вашого Cloudinary cloud |
| apiKey | string | API ключ Cloudinary |
| signature | string | Підпис для авторизації |
| timestamp | number | Unix timestamp |
| folder | string | Шлях до папки (включає ID користувача) |

---

### DELETE /api/upload/image

Видалити зображення за public_id (потрібна автентифікація, тільки власні зображення).

#### Request Body

| Поле | Тип | Обов'язкове | Опис |
|------|-----|-------------|------|
| publicId* | string | Так | Public ID зображення з Cloudinary |

#### Example Request

```json
{
  "publicId": "users/avatars/123e4567-e89b-12d3-a456-426614174000/image123"
}
```

#### Response

**Status**: `200 OK`

#### Error Responses

**403 Forbidden**:
```json
{
  "error": "You can only delete your own images!"
}
```

---

## Security (Безпека)

### Folder Structure

Файли зберігаються в папках, що включають ID користувача:

- Аватари: `users/avatars/{userId}/`
- Фони: `users/backgrounds/{userId}/`
- Пости: `posts/{userId}/`

### Перевірка власності

При видаленні зображень backend перевіряє, чи шлях `public_id` містить ID поточного користувача. Це запобігає видаленню чужих файлів.

### Підпис запитів

Cloudinary вимагає підпису для завантаження файлів. Backend генерує цей підпис з використанням секретного ключа, який ніколи не передається на frontend.

---

## Best Practices

### 1. Валідація на Frontend

Перевіряйте файли перед завантаженням:
- Тип файлу (тільки зображення)
- Розмір файлу (наприклад, максимум 5MB)
- Розміри зображення (рекомендовано)

### 2. Handling Errors

Обробляйте помилки завантаження:

```javascript
try {
  const result = await cloudinary.uploader.upload(file, params);
  // Зберегти result.secure_url
} catch (error) {
  console.error('Upload failed:', error);
  // Показати повідомлення користувачу
}
```

### 3. Progress Indicators

Показуйте прогрес завантаження для покращення UX:

```javascript
cloudinary.uploader.upload(file, {
  ...params,
  onProgress: (progress) => {
    console.log(`Uploaded: ${progress.loaded}%`);
  }
})
```

### 4. Видалення старих файлів

Коли користувач оновлює аватар або фон, видаляйте старе зображення:

```javascript
// 1. Отримати old publicId з поточного avatarUrl
// 2. Завантажити нове зображення
// 3. Оновити профіль з новим URL
// 4. Видалити старе зображення через DELETE /api/upload/image
```

---

## Example Implementation

### React Example

```javascript
import { Cloudinary } from '@cloudinary/url-gen';

async function uploadAvatar(file) {
  // 1. Запит підпису
  const response = await fetch('/api/upload/signature/avatar', {
    headers: {
      'Authorization': `Bearer ${token}`
    }
  });
  const params = await response.json();

  // 2. Завантаження до Cloudinary
  const formData = new FormData();
  formData.append('file', file);
  formData.append('api_key', params.apiKey);
  formData.append('timestamp', params.timestamp);
  formData.append('signature', params.signature);
  formData.append('folder', params.folder);

  const uploadResponse = await fetch(
    `https://api.cloudinary.com/v1_1/${params.cloudName}/image/upload`,
    {
      method: 'POST',
      body: formData
    }
  );
  const result = await uploadResponse.json();

  // 3. Оновлення профілю
  await fetch('/api/user/update', {
    method: 'PATCH',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      avatarUrl: result.secure_url
    })
  });

  return result.secure_url;
}
```

---

## Troubleshooting

### Помилка: "Invalid signature"

- Перевірте, що timestamp не застарілий (дійсний протягом 1 години)
- Переконайтесь, що всі параметри передані правильно

### Помилка: "Upload preset not found"

- Переконайтесь, що folder створена в Cloudinary
- Перевірте налаштування upload preset

### Помилка: 403 Forbidden при видаленні

- Переконайтесь, що publicId містить ID поточного користувача
- Перевірте формат publicId (має включати повний шлях)

---

<div align="center">

**Document Information**  
Version 1.0.0 | Last Updated: January 11, 2026

</div>