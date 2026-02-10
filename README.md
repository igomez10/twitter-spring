# Twitter Clone (Spring Boot)

A simple Twitter-like API built with Spring Boot 3.4.2, Java 21, JPA, and Flyway.

## Requirements

- Java 21
- A PostgreSQL database (for local/dev runtime)

## Quick Start

1. Configure your database connection:

```bash
cp .env.example .env
```

Edit `.env` as needed:

```
DATABASE_URL=jdbc:postgresql://127.0.0.1:5432/postgres
DATABASE_USERNAME=postgres
DATABASE_PASSWORD=postgres
```

2. Run the application:

```bash
./mvnw spring-boot:run
```

## API Endpoints

- `GET /users`
- `GET /actuator/health`
- Swagger UI: `http://localhost:8080/swagger-ui.html`

## Database Migrations

Flyway manages schema and seed data via:

- `src/main/resources/db/migration/V1__initial_schema.sql`
- `src/main/resources/db/migration/V2__seed_data.sql`

On first run, Flyway will create the schema and insert seed data. If you have an existing database, drop it once so Flyway can initialize cleanly.

## Tests

Tests use an in-memory H2 database, so no external DB is required:

```bash
./mvnw clean test
```

## Project Structure

- `src/main/java/com/ignacio/twitter/models` - JPA entities
- `src/main/java/com/ignacio/twitter/controllers` - REST controllers
- `src/main/java/com/ignacio/twitter/repositories` - Spring Data repositories
- `src/main/resources` - application config and Flyway migrations
