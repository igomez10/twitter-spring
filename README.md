# Twitter Clone (Spring Boot + React UI)

Twitter-like API and UI with:
- Spring Boot 3.4.2 backend (Java 21, JPA, Flyway, JWT auth)
- React + Vite + TypeScript frontend in `ui/`
- Unit tests (JUnit + Vitest) and Playwright E2E tests

## Requirements

- Java 21
- Node.js 20+ and npm
- PostgreSQL (for default backend runtime)

## Backend Quick Start (PostgreSQL)

1. Configure your local environment:

```bash
cp .env.example .env
```

Set these values in `.env`:

```
DATABASE_URL=jdbc:postgresql://127.0.0.1:5432/postgres
DATABASE_USERNAME=postgres
DATABASE_PASSWORD=postgres
JWT_SECRET=replace-with-32-char-minimum-secret
```

2. Start dependencies (optional helper):

```bash
make up
```

3. Run backend:

```bash
./mvnw spring-boot:run
```

API base URL: `http://localhost:8084`  
Swagger UI: `http://localhost:8084/swagger-ui.html`

## Frontend Quick Start

1. Install dependencies:

```bash
make ui-install
```

2. Start frontend dev server:

```bash
cd ui && npm run dev
```

Frontend URL: `http://127.0.0.1:4173`

The Vite dev server proxies `/api/*` to `http://127.0.0.1:8084`.
Set `VITE_PROXY_TARGET` before `npm run dev` if your backend runs on a different host or port.

## End-to-End Profile

Playwright starts backend with the `e2e` profile:
- `src/main/resources/application-e2e.properties`
- `src/main/resources/e2e-data.sql`

This profile uses in-memory H2 and preloads authorization data so signup/login users receive the default `basic` role permissions.

## Testing

Backend tests:

```bash
./mvnw test
```

Frontend unit tests:

```bash
make ui-test
```

Playwright tests (Chromium):

```bash
cd ui && npx playwright install chromium
make ui-e2e
```

Note: `ui` E2E scripts run Playwright using `node@22` via `npx` for runner compatibility.

Run full validation:

```bash
make test-all
```

## Project Structure

- `src/main/java/com/ignacio/twitter` - backend source
- `src/main/resources/db/migration` - Flyway migrations
- `src/main/resources/application-e2e.properties` - backend profile for E2E
- `ui/src` - React app
- `ui/tests/e2e` - Playwright tests
