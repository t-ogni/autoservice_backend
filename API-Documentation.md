# 📖 API Документация

## 🔐 Аутентификация

### POST `/register`

**Описание:** Регистрация нового пользователя.

**Request Body:**
```json
{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "securePassword123"
}
```

**Response:**
- 201 Created: `{ "token": "..." }`
- 409 Conflict: "User already exists"

---

### POST `/login`

**Описание:** Авторизация пользователя.

**Request Body:**
```json
{
  "email": "john@example.com",
  "password": "securePassword123"
}
```

**Response:**
- 200 OK: `{ "token": "..." }`
- 401 Unauthorized: "Invalid email or password"

---

### POST `/logout`

**Описание:** Выход пользователя (клиент должен удалить токен).

**Response:**
- 200 OK: "Logged out successfully"

---

## 👤 Пользователи (Users)

> Все действия требуют токен авторизации (`Bearer token`).

---

### POST `/users` (только для **админов**)

**Описание:** Создание нового пользователя администратором.

**Request Body:**
```json
{
  "name": "Jane Doe",
  "email": "jane@example.com",
  "passwordHash": "hashedPassword",
  "role": "user"
}
```
> ⚡ *Передаётся готовый хеш пароля, а не пароль в чистом виде.*

**Response:**
- 201 Created: ID созданного пользователя.

---

### GET `/users`

**Описание:** Получение списка всех пользователей.

**Response:**
- 200 OK: `[ { "id": 1, "name": "...", "email": "...", "role": "user" }, ... ]`

---

### GET `/users/{id}`

**Описание:** Получение данных пользователя по ID.

**Response:**
- 200 OK: `{ "id": 1, "name": "...", "email": "...", "role": "user" }`
- 404 Not Found

---

### PUT `/users/{id}`

**Описание:** Обновление пользователя.
- **Пользователь** может редактировать **только свой профиль**.
- **Админ** может редактировать **любой профиль**.

**Request Body:**
```json
{
  "name": "New Name",
  "email": "new.email@example.com"
}
```
(*любые поля, которые хочешь частично обновить*)

**Response:**
- 200 OK
- 403 Forbidden (если обычный пользователь пытается менять чужую запись)

---

### DELETE `/users/{id}`

**Описание:** Удаление пользователя по ID.

**Response:**
- 200 OK

---

## 🛠️ Услуги (Services)

---

### GET `/services`

**Описание:** Получить список всех услуг.

**Response:**
- 200 OK: `[ { "id": 1, "name": "Oil Change", "price": 2000 }, ... ]`

---

### POST `/services` (только для **админов**)

**Описание:** Добавить новую услугу.

**Request Body:**
```json
{
  "name": "Tire Replacement",
  "description": "Replacing old tires with new ones",
  "price": 5000
}
```

**Response:**
- 201 Created: ID новой услуги.

---

### DELETE `/services/{id}` (только для **админов**)

**Описание:** Удалить услугу по ID.

**Response:**
- 204 No Content

---

## 📝 Заявки (Requests)

---

### POST `/requests`

**Описание:** Создать заявку на услугу.

**Request Body:**
```json
{
  "serviceId": 1,
  "description": "Need urgent tire change"
}
```

**Response:**
- 201 Created: ID новой заявки.

---

### GET `/requests/mine`

**Описание:** Получить все свои заявки.

**Response:**
- 200 OK: `[ { "id": 1, "serviceId": 1, "description": "...", "status": "pending" }, ... ]`

---

### GET `/requests` (только для **админов**)

**Описание:** Получить все заявки всех пользователей.

**Response:**
- 200 OK: список всех заявок.

---

### PUT `/requests/{id}` (только для **админов**)

**Описание:** Обновить статус заявки.

**Request Body:**
```json
{
  "status": "approved",
  "result": "Will be processed tomorrow"
}
```

**Response:**
- 200 OK

---

## 📰 Новости (News)

---

### GET `/news`

**Описание:** Получить все новости.

**Response:**
- 200 OK: `[ { "id": 1, "title": "...", "content": "...", "date": "2025-04-26" }, ... ]`

---

### GET `/news/{id}`

**Описание:** Получить новость по ID.

**Response:**
- 200 OK: одна новость.

---

### POST `/news` (только для **админов**)

**Описание:** Добавить новость.

**Request Body:**
```json
{
  "title": "New Service Launched",
  "content": "We now offer premium oil change!",
  "date": "2025-04-26"
}
```

**Response:**
- 201 Created: ID новости.

---

### PUT `/news/{id}` (только для **админов**)

**Описание:** Обновить новость.

**Request Body:**
```json
{
  "title": "Updated Service Info",
  "content": "Price updated for tire change.",
  "date": "2025-04-27"
}
```

**Response:**
- 200 OK

---

### DELETE `/news/{id}` (только для **админов**)

**Описание:** Удалить новость.

**Response:**
- 204 No Content

---

# 📌 Примечания
- Все защищённые эндпоинты требуют `Authorization: Bearer {token}` в заголовке.
- Срок службы токена - 30 суток
- Роли пользователей: `user`, `admin`.
- Статусы заявок могут быть например: `"pending"`, `"approved"`, `"rejected"`.
- Пароли должны передаваться **в чистом виде только на регистрацию и логин** — в базе они хранятся захешированными.

