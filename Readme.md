![Java](https://img.shields.io/badge/Java-21-blue?logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0-brightgreen?logo=springboot)
![JUnit](https://img.shields.io/badge/JUnit-5-orange?logo=junit5)
![Kafka](https://img.shields.io/badge/Kafka-Event--Driven-green?logo=apachekafka)
![Build](https://img.shields.io/badge/build-passing-brightgreen)

# Milk Order API project using Spring Boot 4, Spring Framework 7 and JUnit 5 compiling with Java 21

REST API built with Spring Boot to manage customers, products, categories and orders.

Designed as a portfolio project to showcase clean architecture, good testing practices, and API development.


## Tech Stack

- Java 21
- Spring Boot 4.0
- Spring Data JPA
- Spring Security (OAuth2 / JWT)
- Kafka (event-driven architecture)
- MySQL & Flyway
- Testcontainers
- Docker (containerization)
- Kubernetes (optional, for local deployment)
- JUnit 5, Mockito
- Open CSV
- Postman


## Architecture Overview

- Controller for HTTP requests and responses
- Service for business use cases
- Repository for data access
- Domain for core business models (rich domain model)
- DTOs and Mappers to separate API contracts from persistence models

The design is inspired by Domain-Driven Design (DDD), focusing on clear boundaries and encapsulated business logic.


## Setup & Configuration

### Getting Started

1. Clone the repository
2. Make sure Docker is running (for MySQL)
3. Run the application:

```
./mvnw spring-boot:run -Dspring-boot.run.profiles=local-mysql
```

The API will be available at http://localhost:8080

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


## Event Publishing (Kafka)

This service publishes domain events to Kafka as part of an event-driven architecture.

For example, when an order is paid, an `order-paid` event is produced and sent to Kafka.

### Requirements

Make sure Kafka and Schema Registry are running.

You can use the infrastructure provided in the
[spring-7-events-consumer](https://github.com/franciscoreinalondon/spring-7-events-consumer) project.

### Configuration

Kafka is enabled via `app.kafka.enabled=true`

Events are serialized using Avro and registered automatically in Schema Registry.

### Notes

- This service acts as a **Kafka producer**
- Events are consumed by an external service (events-consumer)
- If Kafka is disabled (`app.kafka.enabled=false`), events will not be published


## Running with Kubernetes (optional)

This service requires:
- MySQL
- Auth Server

Make sure the [auth server](https://github.com/franciscoreinalondon/spring-7-auth-server) is running in Kubernetes before starting this service.

Build the Docker image:

```
./mvnw clean spring-boot:build-image 
-Dspring-boot.build-image.imageName=spring-7-rest-mvc:0.0.1-SNAPSHOT
```

Deploy MySQL:

```
kubectl apply -f mysql-deployment.yaml
kubectl apply -f mysql-service.yaml
```

Deploy the API:

```
kubectl apply -f rest-mvc-deployment.yaml
kubectl apply -f rest-mvc-service.yaml
```

Expose locally:

```
kubectl port-forward svc/rest-mvc 8080:8080
```

The API will be available at http://localhost:8080


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


## Testing Strategy

- **Unit tests** → validate business logic in isolation (services, mappers, domain helpers)
- **Controller tests** → verify HTTP layer behavior (request/response, validation)
- **Repository tests** → verify JPA queries and persistence behavior using an in-memory database
- **Integration tests** → exercise the full application stack against MySQL with Testcontainers,  
  mocking JWT authentication to focus on functional behavior
- **Postman tests** → validate end-to-end API flows,  
  including real authentication via the auth server and using collection scripts to validate request flows

This combination helps verify both isolated components and real application behavior.

High test coverage (+85% instruction coverage) across services, controllers, and integration layers.

### Code Coverage (JaCoCo)

Run the tests and generate the report:

```
./mvnw clean test jacoco:report
```

Open the report in your browser: `target/site/jacoco/index.html`


## Troubleshooting
- **Port already in use (3306)** → Stop any local MySQL instance or change the Docker port mapping
- **Cannot connect to database** → Ensure Docker is running and the container is up
- **401 Unauthorized** → Verify the auth server is running and a valid token is used
- **Application fails on startup** → Check logs and ensure the correct Spring profile is active


## Future Improvements
- Introduce API documentation (e.g., OpenAPI/Swagger)
- Extend test coverage for edge cases
- Add CI/CD pipeline for automated builds and tests
