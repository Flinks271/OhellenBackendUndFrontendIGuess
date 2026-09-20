# Spring Folder Rules

This project follows a layered and feature-based structure so the backend stays readable as more controllers, services, and entities are added.

## 1. Main package structure

Use this structure under the Java root package:

- `de.ohellen.demo.api`
  - HTTP layer
  - Controllers live here
  - Each feature gets its own subpackage, such as `auth`, `user`, `admin`

- `de.ohellen.demo.application`
  - Business logic and service classes
  - Use case logic belongs here
  - Each feature gets its own subpackage, such as `user`, `auth`

- `de.ohellen.demo.domain`
  - Core domain models and entities
  - Keep persistent entities and domain objects here
  - Example: `domain.user.User`, `domain.user.Role`

- `de.ohellen.demo.infrastructure`
  - Technical implementations and external concerns
  - Includes persistence, security, config, and integrations
  - Example: `infrastructure.persistence`, `infrastructure.security`

- `de.ohellen.demo.shared`
  - Shared helpers, constants, exception classes, and utility code

## 2. Feature-based controller rules

- Put each controller in a feature folder under `api`.
- Keep controller-specific DTOs in a sibling `dto` package inside that feature.
- Example:
  - `api.auth.AuthorizationController`
  - `api.auth.dto.LoginRequest`
  - `api.auth.dto.LoginResponse`

Do not create one giant `controller` package with all DTOs mixed together.

## 3. Layer rules

- `api` packages should only coordinate HTTP requests and responses.
- `application` packages should contain business logic and service orchestration.
- `domain` packages should represent the actual model and core domain rules.
- `infrastructure` packages should contain JPA repositories, security config, JWT helpers, and external integrations.

## 4. Naming conventions

- Controllers end with `Controller`
- Services end with `Service`
- Repositories end with `Repository`
- DTOs should be named by purpose and role, for example `LoginRequest`, `RegisterRequest`, `UserResponse`
- Entity names should be domain nouns, for example `User`, `Role`, `Order`

## 5. Folder rule for new features

When adding a new domain feature, follow this template:

- `api/<feature>/<Feature>Controller.java`
- `api/<feature>/dto/...`
- `application/<feature>/<Feature>Service.java`
- `domain/<feature>/<Feature>.java` or relevant domain objects
- `infrastructure/persistence/<Feature>Repository.java`
- `infrastructure/security/...` only when security-related logic is required

## 6. Example of a clean feature layout

```text
src/main/java/de/ohellen/demo/
├── api/
│   └── auth/
│       ├── AuthorizationController.java
│       └── dto/
│           ├── LoginRequest.java
│           └── RegisterRequest.java
├── application/
│   └── user/
│       └── UserService.java
├── domain/
│   └── user/
│       ├── User.java
│       └── Role.java
├── infrastructure/
│   ├── persistence/
│   │   ├── UserRepository.java
│   │   └── RoleRepository.java
│   └── security/
│       ├── SecurityConfig.java
│       └── jwt/
│           ├── JwtUtil.java
│           └── JwtAuthenticationFilter.java
└── shared/
```

## 7. Principle

Keep each layer responsible for one thing only:

- API handles requests
- Application handles logic
- Domain holds business objects
- Infrastructure handles technical implementations

This keeps the project predictable and easy to extend.
