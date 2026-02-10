# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a **Spring Boot 2.4.5 Twitter Clone** (Java 16) built with Maven. It implements a basic social media API with Users and Tweets as core entities.

## Architecture

The project follows **Spring MVC layered architecture**:

- **Controllers** (`src/main/java/.../controllers/`): REST endpoints that handle HTTP requests
  - `UserController`: Exposes `/users` endpoint to list all users
  - `TweetController`: Currently empty, intended for tweet endpoints

- **Services** (`src/main/java/.../services/`): Business logic layer
  - `UserService`: Interface defining business operations
  - `UserServiceImpl`: Implementation using repository pattern

- **Repositories** (`src/main/java/.../repositories/`): Data access layer using Spring Data JPA
  - `UserRepository` and `TweetRepository` extend `JpaRepository`

- **Models** (`src/main/java/.../models/`): JPA entities
  - `User`: Core user entity with firstName, lastName, email, handle (unique)
  - `Tweet`: Tweet entity with content, author (User), timestamp

- **Configurations** (`src/main/java/.../configurations/`): Spring beans and initialization
  - `UserConfig` and `TweetConfig`: CommandLineRunner beans for seed data

## Database Configuration

Database settings are in `src/main/resources/application.properties`:
- **Production**: PostgreSQL at `jdbc:postgresql://127.0.0.1:5432/postgres`
- **Development**: H2 database available as alternative (add profile or change properties)
- **ORM**: Hibernate with DDL set to `create` (drops and recreates schema on startup)
- **Show SQL**: Enabled for debugging

Note: `application.properties` contains hardcoded credentials (`postgres:postgres`). Consider externalizing for production.

## Build & Run Commands

```bash
# Build the project
./mvnw clean package

# Run the application
./mvnw spring-boot:run

# Run tests
./mvnw test

# Run a specific test class
./mvnw test -Dtest=TweetControllerTest

# Run a specific test method
./mvnw test -Dtest=TweetControllerTest#testMethodName
```

## Common Development Tasks

**Start PostgreSQL** (if not running):
```bash
# Ensure PostgreSQL is running on localhost:5432
psql -U postgres
```

**Check endpoints** after running the application:
- `GET /users` - List all users (implemented in UserController)

**Add a new endpoint**:
1. Add method to corresponding controller with `@GetMapping`, `@PostMapping`, etc.
2. Implement business logic in service layer
3. Update repository if custom queries needed
4. Add test in `src/test/java/.../`

## Key Dependencies

- **spring-boot-starter-data-jpa**: ORM and database abstraction
- **spring-boot-starter-web**: REST controller and embedded Tomcat
- **postgresql** and **h2**: Database drivers
- **spring-boot-starter-test**: JUnit and testing utilities

## Testing

Currently minimal test coverage. Test files are in `src/test/java/.../`:
- `TweetControllerTest.java`: Empty, ready for implementation
- `TwitterApplicationTests.java`: Basic Spring context test

Use `@SpringBootTest` for integration tests or `@WebMvcTest` for controller unit tests.

## Notes on Current Code

- **Tweet model**: Uses `@OneToOne` relationship with User (author). Consider `@ManyToOne` if users can have multiple tweets.
- **ID naming inconsistency**: Tweet uses `ID` (camelCase variant) while User uses `id`. Standardize for consistency.
- **Incomplete endpoints**: TweetController exists but has no mapped methods. Tweets can't be created/retrieved via API yet.
- **No validation**: Models lack constraint annotations (`@NotNull`, `@Size`, etc.). Add for production use.
- **No pagination**: `listUsers()` returns all users. Add pagination for scalability.
