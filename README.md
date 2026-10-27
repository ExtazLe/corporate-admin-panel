# Corp Admin Panel

Админ-панель корпоративного сервиса на **Java 17 + Spring Boot 3** с гибкой настройкой прав доступа (RBAC):
пользователи → роли → права (permissions). Права назначаются ролям, роли — пользователям, всё через REST API.

## Возможности

- Управление пользователями: создание, просмотр, блокировка/разблокировка, удаление, назначение ролей
- Управление ролями: создание, настройка набора прав, удаление (роль `ADMIN` защищена)
- Проверка прав на уровне методов (`@PreAuthorize("hasAuthority('USER_WRITE')")`)
- Политики паролей (длина, сложность), расширяемые без изменения кода
- Журнал аудита всех административных действий
- Веб-интерфейс без сборки (vanilla JS)
- Единый формат ошибок (`ProblemDetail`, RFC 7807)

## Стек

Java 17, Spring Boot 3.3 (Web, Security, Data JPA, Validation), H2 (in-memory), JUnit 5, Maven.

## Быстрый старт

```bash
git clone https://github.com/<ExtazLe>/corp-admin-panel.git
cd corp-admin-panel
mvn spring-boot:run
```

Приложение стартует на `http://localhost:8080`. При первом запуске создаётся администратор:

| Логин | Пароль по умолчанию |
|-------|---------------------|
| `admin` | `Admin#12345` (переопределяется переменной окружения `ADMIN_PASSWORD`) |

> ⚠️ Смените пароль и подключите production-БД (PostgreSQL) перед боевым использованием.

Тесты: `mvn test`

## Веб-интерфейс

После запуска откройте **http://localhost:8080** и войдите как `admin`.

- **Пользователи** — создание, назначение ролей, блокировка, удаление
- **Роли и права** — создание ролей и настройка набора прав галочками
- **Журнал аудита** — кто, что и когда изменил

Разделы и кнопки показываются в зависимости от прав вошедшего пользователя (данные берутся из `GET /api/me`),
а реальная проверка всё равно выполняется на сервере. Интерфейс — статика в `src/main/resources/static`
(HTML + CSS + JS без сборки и зависимостей), поддерживает светлую и тёмную тему.

> Учётные данные хранятся в `sessionStorage` вкладки до выхода. Для production лучше перейти на JWT/сессии с HttpOnly-cookie.

## Права доступа

| Permission | Что разрешает |
|------------|---------------|
| `USER_READ` | просмотр пользователей |
| `USER_WRITE` | создание, смена ролей, блокировка |
| `USER_DELETE` | удаление пользователей |
| `ROLES_READ` | просмотр ролей и списка прав |
| `ROLES_WRITE` | создание/изменение/удаление ролей |
| `AUDIT_READ` | просмотр журнала аудита |

Стартовые роли: `ADMIN` (все права), `MANAGER` (чтение пользователей, ролей, аудита), `EMPLOYEE` (без прав).

## REST API

Аутентификация — HTTP Basic.

| Метод | URL | Право |
|-------|-----|-------|
| GET | `/api/users`, `/api/users/{id}` | `USER_READ` |
| POST | `/api/users` | `USER_WRITE` |
| PUT | `/api/users/{id}/roles` | `USER_WRITE` |
| PATCH | `/api/users/{id}/enabled?value=true\|false` | `USER_WRITE` |
| DELETE | `/api/users/{id}` | `USER_DELETE` |
| GET | `/api/roles`, `/api/permissions` | `ROLES_READ` |
| POST / PUT / DELETE | `/api/roles`, `/api/roles/{id}` | `ROLES_WRITE` |
| GET | `/api/audit` | `AUDIT_READ` |

Примеры:

```bash
# создать роль
curl -u admin:'Admin#12345' -X POST localhost:8080/api/roles \
  -H 'Content-Type: application/json' \
  -d '{"name":"HR","permissions":["USER_READ","USER_WRITE"]}'

# создать пользователя с ролью
curl -u admin:'Admin#12345' -X POST localhost:8080/api/users \
  -H 'Content-Type: application/json' \
  -d '{"username":"ivan","password":"Str0ng#Pass99","roleIds":[4]}'

# журнал аудита
curl -u admin:'Admin#12345' localhost:8080/api/audit
```

## Архитектура

```
controller  →  service (интерфейсы + реализации)  →  repository  →  domain
                   ↓            ↓             ↓
               factory      policy        event (audit)
security (UserDetailsService, SecurityConfig)
```

## SOLID

| Принцип | Где применён |
|---------|--------------|
| **S** — Single Responsibility | Контроллеры только маршрутизируют, сервисы — бизнес-логика, `UserFactory` — создание, `AuditEventListener` — аудит, `PasswordValidator` — проверка паролей |
| **O** — Open/Closed | Новая политика пароля = новый `@Component implements PasswordPolicy`; `PasswordValidator` не меняется. Новый подписчик на `AuditEvent` — без правок сервисов |
| **L** — Liskov Substitution | Любая реализация `UserService`/`RoleService`/`PasswordPolicy` взаимозаменяема |
| **I** — Interface Segregation | Узкие интерфейсы: `UserService`, `RoleService`, `PasswordPolicy`, раздельные репозитории |
| **D** — Dependency Inversion | Сервисы и контроллеры зависят от абстракций, зависимости внедряются через конструктор |

## Паттерны проектирования

| Паттерн | Реализация |
|---------|-----------|
| **Strategy** | `PasswordPolicy` + `MinLengthPolicy`, `ComplexityPolicy` |
| **Factory** | `UserFactory` — единая точка создания пользователей с хешированием пароля |
| **Observer** | `AuditEvent` + `AuditEventListener` (Spring Events) |
| **Facade** | `AuditPublisher` скрывает определение актора и публикацию события |
| **Adapter** | `DbUserDetailsService` адаптирует доменного `User` к `UserDetails` |
| **Repository** | Spring Data JPA репозитории |
| **DTO** | Java records для запросов/ответов, сущности наружу не отдаются |

## Идеи для развития

- JWT или сессии вместо HTTP Basic, PostgreSQL + Flyway
- Пагинация и фильтры в списках, Swagger/OpenAPI
- Docker и CI (GitHub Actions)

## Лицензия

MIT
