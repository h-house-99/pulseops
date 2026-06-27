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
export PULSEOPS_CORS_ALLOWED_ORIGINS=http://localhost:5173
export PULSEOPS_READ_ONLY_MODE=false
export PULSEOPS_SEED_CURATED_MONITORS=false
```

`PULSEOPS_CORS_ALLOWED_ORIGINS` controls which frontend origin may call the API from the browser:

- Local default: `http://localhost:5173`
- Production: set this to your deployed frontend URL, for example `https://pulseops.example.com`

`PULSEOPS_READ_ONLY_MODE` controls backend write access:

- `false` (default): create, manual check, and delete endpoints are allowed.
- `true`: those write endpoints return `403`, but `GET` endpoints and scheduled checks still work.

`PULSEOPS_SEED_CURATED_MONITORS` controls startup seeding:

- `false` (default): no monitors are inserted automatically.
- `true`: inserts the curated monitor list when the database has no monitors yet.

For the public demo on a fresh database, typical backend values are:

```bash
export PULSEOPS_CORS_ALLOWED_ORIGINS=https://your-frontend.example.com
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
| Run CORS tests | `./mvnw test -Dtest=CorsConfigIntegrationTest` |

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

The frontend runs at `http://localhost:5173` by default. The API base URL comes from `VITE_API_BASE_URL`.

Copy the frontend example environment file for local UI mode:

```bash
cp frontend/.env.example frontend/.env.local
```

Example local frontend values:

```bash
VITE_API_BASE_URL=http://localhost:8080/api
VITE_CAN_MANAGE_MONITORS=true
```

`VITE_API_BASE_URL` controls where the frontend sends API requests:

- Local default: `http://localhost:8080/api`
- Production: set this before `npm run build`, for example `https://your-backend.example.com/api`

`VITE_CAN_MANAGE_MONITORS` controls whether the dashboard shows create, check, and delete controls:

- `true`: show Admin controls for local development.
- `false`: show Viewer mode for the public demo UI.

This flag only affects the UI. Backend write protection still depends on `PULSEOPS_READ_ONLY_MODE`.

Vite env vars are baked in at build time. Set production frontend env vars in the hosting platform before running `npm run build`.

Example production frontend build values:

```bash
VITE_API_BASE_URL=https://your-backend.example.com/api
VITE_CAN_MANAGE_MONITORS=false
```

## Full Stack Startup

1. Start PostgreSQL and create the `pulseops` database if needed.
2. Start the backend from `backend/` with `./mvnw spring-boot:run`.
3. Start the frontend from `frontend/` with `npm run dev`.
4. Open `http://localhost:5173`.

## Production Deployment

Local setup above is for development. Production uses the same environment variable names with deployed URLs and demo flags.

For the full production topology — Render services, Docker backend, Postgres, CORS, read-only mode, seeding, keep-alive cron, and request flow — see [Architecture → Production Deployment Architecture](ARCHITECTURE.md#production-deployment-architecture).

Quick production checklist:

1. Create Render Postgres and copy JDBC credentials into the backend service.
2. Deploy the backend as a Docker web service from `backend/Dockerfile`.
3. Deploy the frontend as a static site with `VITE_API_BASE_URL` pointing at the backend `/api` URL.
4. Set backend demo flags: `PULSEOPS_READ_ONLY_MODE=true`, `PULSEOPS_SEED_CURATED_MONITORS=true`, and `PULSEOPS_CORS_ALLOWED_ORIGINS` to the frontend URL.
5. Configure an external cron job to `GET /api/health` every 5–10 minutes so free-tier web services stay awake.
