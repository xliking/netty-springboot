# Netty Spring Boot Demo

This project demonstrates how to integrate a Netty-based WebSocket server with a Spring Boot 3 application.
It includes sample chat and friend management services backed by MyBatis-Plus, Redis and Sa-Token.

## Features
- Netty WebSocket server configured via `application.yml`
- RESTful APIs for chat, groups and friends
- Sa-Token session management with Redis
- MyBatis-Plus for database access
- Actuator and Thymeleaf support

## Prerequisites
- JDK 17+
- Maven 3+
- Running MySQL and Redis instances. Default settings are defined in `src/main/resources/application.yml`.
- (Optional) Initialize the database using `init.sql`.

## Build & Run
```bash
mvn spring-boot:run
```
The HTTP server listens on port **8081** and the Netty WebSocket server on port **8090** by default.

## Testing
Execute unit tests (none provided yet):
```bash
mvn test
```

## Project Layout
- `src/main/java` – application sources
- `src/main/resources` – configuration, mapper XMLs and templates
- `init.sql` – sample database schema

## License
Distributed under the MIT License.
