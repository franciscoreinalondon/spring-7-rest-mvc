![Java](https://img.shields.io/badge/Java-21-blue?logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0-brightgreen?logo=springboot)
![JUnit](https://img.shields.io/badge/JUnit-5-orange?logo=junit5)
![Build](https://img.shields.io/badge/build-passing-brightgreen)

# Milk Order API project using Spring Boot 4, Spring Framework 7 and JUnit 5 compiling with Java 21

REST API built with Spring Boot to manage customers, products, categories and orders.

Designed as a portfolio project to showcase clean architecture, good testing practices, and API development.

---

## Tech Stack

- Java 21
- Spring Boot 4.0
- Spring Data JPA
- Spring Security (OAuth2 / JWT)
- MySQL & Flyway
- Testcontainers
- JUnit 5, Mockito
- Open CSV
- Postman

---

## Architecture Overview

- Controller for HTTP requests and responses
- Service for business use cases
- Repository for data access
- Domain for core business models (rich domain model)
- DTOs and Mappers to separate API contracts from persistence models

The design is inspired by Domain-Driven Design (DDD), focusing on clear boundaries and encapsulated business logic.

---

## Setup & Configuration

### Getting Started

1. Clone the repository
2. Make sure Docker is running (for MySQL)
3. Run the application:

```
./mvnw spring-boot:run -Dspring-boot.run.profiles=local-mysql
```

The API will be available at http://localhost:8080.

### Profiles

The application uses different Spring profiles for specific use cases:

- `local-mysql` → runs the app against a local MySQL instance (Docker Compose)
- `integration-test` → used for integration tests with Testcontainers

### Docker & Local Environment

Docker Compose is used to run a local MySQL instance.

The database is automatically started when running the application with the `local-mysql` profile.

### Database (Flyway)

Database schema is managed using Flyway.

Migrations are automatically applied on application startup, ensuring the database is always up to date.

### Security (OAuth2 / Auth Server)

The API is secured using OAuth2 with JWT tokens.

Authentication is handled by an external authorization server 
[spring-7-auth-server](https://github.com/franciscoreinalondon/spring-7-auth-server), 
which runs at http://localhost:9000 and must be started to obtain valid access tokens.

---

## API Endpoints

The API exposes endpoints for managing customers, milk products, categories, and orders.

All endpoints are prefixed with: `http://localhost:8080/api/v1`

### Example: Milk endpoints

- POST `/milks`
- GET `/milks`
- GET `/milks/{milkId}`
- PUT `/milks/{milkId}`
- PATCH `/milks/{milkId}`
- DELETE `/milks/{milkId}`

A complete and ready-to-use 
[Postman collection](https://github.com/franciscoreinalondon/spring-7-rest-mvc/tree/main/postman) 
is included in this repository.

---

## Testing Strategy

- **Unit tests** → validate business logic in isolation (services, mappers, domain helpers)
- **Controller tests** → verify HTTP layer behavior (request/response, validation)
- **Repository tests** → verify JPA queries and persistence behavior using an in-memory database
- **Integration tests** → exercise the full application stack against MySQL with Testcontainers,  
  mocking JWT authentication to focus on functional behavior
- **Postman tests** → validate end-to-end API flows,  
  including real authentication via the auth server and using collection scripts to validate request flows

This combination helps verify both isolated components and real application behavior.

---

## Troubleshooting
- **Port already in use (3306)** → Stop any local MySQL instance or change the Docker port mapping
- **Cannot connect to database** → Ensure Docker is running and the container is up
- **401 Unauthorized** → Verify the auth server is running and a valid token is used
- **Application fails on startup** → Check logs and ensure the correct Spring profile is active

---

## Future Improvements
- Introduce API documentation (e.g., OpenAPI/Swagger)
- Extend test coverage for edge cases
- Add CI/CD pipeline for automated builds and tests
