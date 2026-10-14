# Corporate Admin Panel

Админ-панель корпоративного сервиса с гибкой настройкой прав доступа (RBAC) на Spring Boot.

## Стек технологий

- Java 21, Spring Boot 3.3.5
- Spring Security 6 + JWT (JJWT 0.12.5)
- Spring Data JPA + Hibernate 6
- H2 Database (in-memory, для разработки)
- SpringDoc OpenAPI (Swagger UI)
- Lombok, Maven

## Возможности

- Аутентификация по JWT
- RBAC: пользователи — роли — разрешения
- Иерархия ролей (ADMIN — MANAGER — USER)
- Метод-уровневая защита через @PreAuthorize
- Аудит изменений прав (паттерн Observer)
- Глобальная обработка ошибок
- Swagger UI и H2-консоль

## Архитектура

Controller -> Service -> Repository -> Entity

- Controller: REST-эндпоинты, валидация DTO
- Service: бизнес-логика, @PreAuthorize
- Repository: Spring Data JPA
- Security: JWT-фильтр, UserDetailsService, JwtService
- Event: Observer для аудита изменений прав

## Быстрый старт

Требования:

- JDK 21 (Eclipse Temurin или Amazon Corretto)
- Maven (или используй wrapper mvnw)

Запуск:

    git clone https://github.com/ExtazLe/corporate-admin-panel.git
    cd corporate-admin-panel
    ./mvnw spring-boot:run

Для Windows PowerShell:

    .\mvnw.cmd spring-boot:run

Приложение: http://localhost:8080

## Доступы по умолчанию

| Логин | Пароль   | Роль       |
|-------|----------|------------|
| admin | admin123 | ROLE_ADMIN |

В продакшене обязательно смени пароль и JWT-секрет.

## Эндпоинты

| Метод  | URL                          | Доступ        | Описание                 |
|--------|------------------------------|---------------|--------------------------|
| POST   | /api/auth/login              | публично      | Логин, возвращает JWT    |
| POST   | /api/auth/register           | публично      | Регистрация              |
| GET    | /api/users                   | USER_READ     | Список пользователей     |
| GET    | /api/users/{id}              | USER_READ     | Пользователь по ID       |
| PUT    | /api/users/{id}              | USER_WRITE    | Обновить пользователя    |
| DELETE | /api/users/{id}              | USER_DELETE   | Удалить пользователя     |
| PUT    | /api/users/{id}/roles        | ROLE_MANAGE   | Назначить роли           |
| GET    | /api/roles                   | USER_READ     | Список ролей             |
| POST   | /api/roles                   | ROLE_MANAGE   | Создать роль             |
| PUT    | /api/roles/{id}/permissions  | ROLE_MANAGE   | Обновить права роли      |
| DELETE | /api/roles/{id}              | ROLE_MANAGE   | Удалить роль             |
| GET    | /api/admin/dashboard         | ADMIN_PANEL   | Приветствие админа       |

## Swagger UI

http://localhost:8080/swagger-ui/index.html

Как пользоваться:

1. Вызвать POST /api/auth/login с admin/admin123
2. Скопировать token из ответа
3. Нажать Authorize справа сверху
4. Вставить Bearer <token>
5. Вызывать защищённые эндпоинты

## H2 Console

http://localhost:8080/h2-console

| Поле      | Значение                 |
|-----------|--------------------------|
| JDBC URL  | jdbc:h2:mem:admin_panel  |
| User Name | sa                       |
| Password  | (пусто)                  |

## Пример использования

Логин:

    curl -X POST http://localhost:8080/api/auth/login \
      -H "Content-Type: application/json" \
      -d '{"username":"admin","password":"admin123"}'

Ответ:

    {"token":"eyJhbGci...","username":"admin","roles":["ROLE_ADMIN"]}

Использование токена:

    curl http://localhost:8080/api/admin/dashboard \
      -H "Authorization: Bearer <TOKEN>"

Ответ:

    {"message":"Добро пожаловать в админ-панель","status":"OK"}

## Принципы и паттерны

| Категория        | Что применено                                              |
|------------------|------------------------------------------------------------|
| SOLID            | SRP, OCP, LSP, ISP, DIP                                    |
| Паттерны GoF     | Strategy, Observer, Builder, Facade, Singleton             |
| Архитектурные    | Service Layer, DTO, Repository, Global Exception Handler   |

## Структура проекта

    src/main/java/com/company/corporate_admin_panel/
      CorporateAdminPanelApplication.java
      config/       SecurityConfig, OpenApiConfig, DataInitializer
      controller/   REST-контроллеры
      service/      Бизнес-логика
      repository/   Spring Data JPA
      entity/       User, Role, Permission
      dto/          Request/Response объекты
      security/     JWT, UserDetailsService, фильтр
      event/        Observer для аудита
      exception/    Глобальный обработчик ошибок

## Тесты

    ./mvnw test

## Лицензия

MIT
