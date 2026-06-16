# Setup

This guide covers local development for the PulseOps frontend, backend, and database.

## Prerequisites

- Java 26 or a compatible JDK for the current Spring Boot setup
- Node.js 20+ and npm
- PostgreSQL

## Database

Local development expects PostgreSQL on `localhost:5432` with a database named `pulseops`.

```bash
createdb pulseops
```

The backend reads database settings from Spring configuration. Copy the example environment file and fill in local values:

```bash
cp backend/.env.example backend/.env
```

Example environment values:

```bash
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/pulseops
export SPRING_DATASOURCE_USERNAME=your_postgres_username
export SPRING_DATASOURCE_PASSWORD=your_postgres_password
export PULSEOPS_READ_ONLY_MODE=false
export PULSEOPS_SEED_CURATED_MONITORS=false
```

`PULSEOPS_READ_ONLY_MODE` controls backend write access:

- `false` (default): create, manual check, and delete endpoints are allowed.
- `true`: those write endpoints return `403`, but `GET` endpoints and scheduled checks still work.

`PULSEOPS_SEED_CURATED_MONITORS` controls startup seeding:

- `false` (default): no monitors are inserted automatically.
- `true`: inserts the curated monitor list when the database has no monitors yet.

For the public demo on a fresh database, typical values are:

```bash
export PULSEOPS_SEED_CURATED_MONITORS=true
export PULSEOPS_READ_ONLY_MODE=true
```

Spring Boot creates or updates the `monitors` and `check_results` tables on startup using Hibernate.

If you run the backend from a terminal, load the local environment file before starting Spring Boot:

```bash
cd backend
set -a
source .env
set +a
./mvnw spring-boot:run
```

`set -a` exports the variables loaded from `.env` so Spring Boot can read them.

If you run from VS Code or Cursor, configure the Java launch profile to use `backend/.env` as its `envFile` instead of sourcing it manually each time.

Useful PostgreSQL commands:

| Task | Command |
|------|---------|
| Check if Postgres is running | `pg_isready -h localhost -p 5432` |
| Start with Homebrew | `brew services start postgresql@16` |
| Stop with Homebrew | `brew services stop postgresql@16` |
| Connect to the database | `psql -h localhost -p 5432 -d pulseops` |
| List databases | `psql -h localhost -p 5432 -l` |

Replace `postgresql@16` with your installed PostgreSQL version if needed.

## Backend

Run backend commands from `backend/`.

| Task | Command |
|------|---------|
| Start API | `./mvnw spring-boot:run` |
| Run tests | `./mvnw test` |
| Compile | `./mvnw compile` |
| Package JAR | `./mvnw package` |
| Run one test class | `./mvnw test -Dtest=MonitorApiIntegrationTest` |
| Run read-only tests | `./mvnw test -Dtest=MonitorApiReadOnlyIntegrationTest` |
| Run seeding tests | `./mvnw test -Dtest=MonitorApiSeedCuratedMonitorIntegrationTest` |

The API runs at `http://localhost:8080` by default.

When the backend is running, scheduled monitor checks run every 5 minutes and write new check results to the database.

Automated tests use an in-memory H2 database configured in `backend/src/test/resources/application.properties`.

## Frontend

Run frontend commands from `frontend/`.

| Task | Command |
|------|---------|
| Install dependencies | `npm install` |
| Start dev server | `npm run dev` |
| Lint | `npm run lint` |
| Production build | `npm run build` |
| Preview production build | `npm run preview` |

The frontend runs at `http://localhost:5173` by default and calls the backend at `http://localhost:8080/api`.

Copy the frontend example environment file for local UI mode:

```bash
cp frontend/.env.example frontend/.env.local
```

Example frontend values:

```bash
VITE_CAN_MANAGE_MONITORS=true
```

`VITE_CAN_MANAGE_MONITORS` controls whether the dashboard shows create, check, and delete controls:

- `true`: show Admin controls for local development.
- `false`: show Viewer mode for the public demo UI.

This flag only affects the UI. Backend write protection still depends on `PULSEOPS_READ_ONLY_MODE`.

## Full Stack Startup

1. Start PostgreSQL and create the `pulseops` database if needed.
2. Start the backend from `backend/` with `./mvnw spring-boot:run`.
3. Start the frontend from `frontend/` with `npm run dev`.
4. Open `http://localhost:5173`.
