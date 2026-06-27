# Architecture

PulseOps is a full-stack API monitoring dashboard with a React frontend, a Spring Boot backend, and PostgreSQL persistence.

## Components

```text
React + Vite frontend
  -> Spring Boot REST API
    -> PostgreSQL
    -> External API endpoints
```

The frontend manages the monitor dashboard, user input, loading states, monitor summary statistics, mapped failure reasons, latency charts, chart point details, per-window chart caching, and periodic dashboard refreshes.

The backend owns monitor validation, persistence, endpoint health checks, scheduled background checks, summary-stat calculation, and API response shaping.

PostgreSQL stores monitored endpoints and historical check results. Tests use H2 so backend integration tests can run without a local Postgres instance.

## Manual Check Flow

1. A user adds a monitor from the React dashboard.
2. The frontend sends `POST /api/monitors`.
3. Spring Boot validates and stores the monitor with status `UNKNOWN`.
4. A user clicks check now.
5. The backend calls the monitor URL, records status code, latency, checked time, and any request error.
6. The backend recalculates monitor summary fields from stored check results.
7. The frontend refreshes the monitor list and displays status, latency summaries, latest error details, selected-window chart stats, and latency chart data.

## Scheduled Check Flow

1. Spring scheduling is enabled when the backend starts.
2. `MonitorCheckScheduler` runs every 5 minutes.
3. The scheduler asks `MonitorService` to check all stored monitors.
4. Each result updates the monitor's latest status and creates a `CheckResult` history row.
5. The React dashboard polls the monitor list every 60 seconds, so newly scheduled results appear without a manual refresh.

## Dashboard Refresh And Chart Caching

1. The frontend polls `GET /api/monitors` every 60 seconds to refresh live monitor status and summary fields.
2. When a monitor card is expanded, the frontend fetches chart history with `GET /api/monitors/{id}/checks?hours=...`.
3. Chart results are cached in memory using a composite key of `monitorId` plus `windowHours`.
4. Shorter windows (`1`, `8`, `24`) are refreshed on dashboard poll so expanded charts stay live.
5. The `7d` window (`hours=168`) uses a 1-hour frontend TTL so repeated window toggles and polls do not refetch large history payloads unnecessarily.

## Production Deployment Architecture

PulseOps is deployed on [Render](https://render.com) as three separate services plus an external keep-alive job. The browser never talks to PostgreSQL directly; all dashboard data flows through the backend API.

```text
User browser
  -> Static Site (React build)
       -> Web Service (Spring Boot in Docker)
            -> Render Postgres
            -> External monitor URLs (GitHub, status pages, etc.)

External cron (cron-job.org)
  -> GET /api/health on the backend every 5 minutes
```

### Render services

| Service | Type | Role |
| --- | --- | --- |
| Frontend | Static Site | Serves the Vite production build (`index.html`, JS, CSS) |
| Backend | Web Service (Docker) | Runs `backend/Dockerfile`, exposes `/api/*` on port `8080` |
| Database | Managed Postgres | Stores `monitors` and `check_results` |

Live demo URLs:

- Frontend: `https://pulseops-u82b.onrender.com/`
- Backend: `https://pulseops-api.onrender.com`

The frontend static site and backend web service are **separate Render services**. When a visitor opens the dashboard, JavaScript in the browser calls the backend API over HTTPS. CORS must allow the frontend origin exactly.

### Backend Docker image

The backend image is built from `backend/Dockerfile`:

1. Build stage: Eclipse Temurin JDK 21 runs `./mvnw clean package`.
2. Run stage: Eclipse Temurin JRE 21 runs the packaged JAR.
3. Render sets `PORT`; Spring reads it through `server.port=${PORT:8080}`.

### Scheduled checks vs keep-alive

Two different 5-minute schedules serve different purposes:

| Job | Where it runs | Purpose |
| --- | --- | --- |
| `MonitorCheckScheduler` | Inside the Spring Boot JVM | Checks all monitors and deletes check results older than 30 days |
| External health ping | cron-job.org (or similar) | Sends inbound HTTP to `/api/health` so Render free-tier web services stay awake |

On Render's free tier, a web service sleeps after roughly 15 minutes with **no inbound HTTP traffic**. Outbound monitor checks do **not** keep the service awake. If the JVM sleeps, the internal scheduler stops until the next inbound request triggers a cold start.

Recommended keep-alive target:

```text
GET https://pulseops-api.onrender.com/api/health
```

Run the ping every 5–10 minutes so the backend stays warm and scheduled monitor checks continue reliably.

### Production request flow

1. The browser loads static assets from the frontend URL.
2. React polls `GET /api/monitors` every 60 seconds against `VITE_API_BASE_URL`.
3. Spring Boot reads monitors and summary stats from Postgres.
4. Every 5 minutes, `MonitorCheckScheduler` calls each monitor URL through `RestClientEndpointCheckClient`.
5. Each check stores a `CheckResult` row and updates the monitor's latest status fields.
6. Expanded monitor cards fetch chart history with `GET /api/monitors/{id}/checks?hours=...`.

### Production environment variables

| Service | Variable | Example / notes |
| --- | --- | --- |
| Frontend | `VITE_API_BASE_URL` | `https://pulseops-api.onrender.com/api` (set before `npm run build`) |
| Frontend | `VITE_CAN_MANAGE_MONITORS` | `false` for the public demo |
| Backend | `SPRING_DATASOURCE_URL` | JDBC URL from Render Postgres internal connection string |
| Backend | `SPRING_DATASOURCE_USERNAME` | From Render Postgres credentials |
| Backend | `SPRING_DATASOURCE_PASSWORD` | From Render Postgres credentials |
| Backend | `PULSEOPS_CORS_ALLOWED_ORIGINS` | `https://pulseops-u82b.onrender.com` (no port, no trailing slash) |
| Backend | `PULSEOPS_READ_ONLY_MODE` | `true` |
| Backend | `PULSEOPS_SEED_CURATED_MONITORS` | `true` on a fresh database |

Local development uses the same variable names with localhost values. See [Setup](SETUP.md) for copy-paste examples.

### Free-tier constraints

Render free tier is suitable for a resume demo, but it has tradeoffs worth documenting:

- Web services sleep without inbound traffic unless an external pinger is configured.
- Free Postgres instances can expire or have storage limits; check Render's current pricing page before relying on them long term.
- Cold starts after sleep can add 30–60+ seconds to the first request.
- Shared cloud egress IPs can cause intermittent `HTTP 403` responses from APIs such as GitHub that rate-limit datacenter traffic.

Self-hosting on always-on hardware (for example a home Linux box) removes sleep and database expiry concerns, but adds networking and ops work. Render remains the simpler first deployment path.

## Endpoint Check Error Mapping

Monitor checks use Java's `HttpClient` through Spring `RestClient` with a **5 second connect timeout** and **5 second read timeout**.

`RestClientEndpointCheckClient` maps common network failures to stored `errorMessage` values. `FailureReasonMapper` then converts those into user-facing `latestFailureReason` labels on monitor cards.

| Root cause / condition | Stored `errorMessage` | User-facing reason |
| --- | --- | --- |
| HTTP status outside `200–399` | `null` (status code stored instead) | `HTTP {code}` |
| Unknown host / unresolved address | `DNS resolution failed` | `DNS resolution failed` |
| Connection refused | `Connection refused` | `Connection refused` |
| `SocketTimeoutException` or `HttpTimeoutException` | `Request timed out` | `Request timed out` |
| SSL / certificate problem | `TLS certificate expired` | `TLS certificate expired` |
| Unexpected EOF | `Connection closed unexpectedly` | `Connection closed unexpectedly` |
| Cancelled request message | `Request cancelled` | `Request cancelled` |
| Anything else | `Request failed` | `Request failed` |

JDK `HttpClient` timeouts throw `HttpTimeoutException`, not `SocketTimeoutException`. Both are mapped to `Request timed out` so production checks do not fall through to the unmapped warning path.

Unmapped failures still log a warning with exception details for debugging:

```text
Unmapped endpoint check failure for url: ...
```

## Deployment Configuration

PulseOps uses environment-driven configuration for the public demo and local development.

| Layer | Flag | Purpose |
| --- | --- | --- |
| Frontend | `VITE_API_BASE_URL` | Sets the backend API base URL used by dashboard fetches |
| Frontend | `VITE_CAN_MANAGE_MONITORS` | Hides create, check, and delete UI actions |
| Backend | `PULSEOPS_CORS_ALLOWED_ORIGINS` | Allows the deployed frontend origin to call the API from the browser |
| Backend | `PULSEOPS_READ_ONLY_MODE` | Blocks write API calls with `403 Forbidden` |
| Backend | `PULSEOPS_SEED_CURATED_MONITORS` | Seeds curated monitors when the database is empty |

Backend flags are read at startup through `PulseOpsConfig`. Frontend flags are read by Vite at dev-server startup or production build time.

## Read-Only Demo Mode

Read-only enforcement lives in `MonitorService`. User-triggered write methods call `ensureMonitorManagementAllowed()` before creating monitors, running manual checks, or deleting monitors.

When read-only mode is enabled:

- `POST /api/monitors`, `POST /api/monitors/{id}/check-now`, and `DELETE /api/monitors/{id}` are rejected.
- `GET /api/monitors`, chart history endpoints, health checks, and scheduled background checks continue to work.

Local development usually keeps backend read-only off, seeding off, and frontend management UI on. The deployed demo should set:

- `VITE_API_BASE_URL` to the production backend URL
- `VITE_CAN_MANAGE_MONITORS=false`
- `PULSEOPS_CORS_ALLOWED_ORIGINS` to the production frontend URL
- `PULSEOPS_READ_ONLY_MODE=true`
- `PULSEOPS_SEED_CURATED_MONITORS=true` on a fresh database

## Curated Monitor Catalog And Seeding

PulseOps keeps one curated API list in `CuratedMonitorDefinition`. That list is reused in two places:

| Consumer | Purpose |
| --- | --- |
| `MonitorDataSeeder` | Inserts monitors into PostgreSQL when `PULSEOPS_SEED_CURATED_MONITORS=true` and the database is empty |
| `PublicApiService` | Returns the same catalog from `GET /api/public-apis` |

Seeding flow:

1. Spring Boot starts and the application context is ready.
2. `MonitorDataSeeder` runs as an `ApplicationRunner`.
3. If seeding is disabled, it exits immediately.
4. If monitors already exist, it exits without inserting duplicates.
5. Otherwise it inserts the curated monitors with `status = "UNKNOWN"`.
6. `MonitorCheckScheduler` checks them on the normal 5-minute schedule.

Backend demo flags are managed together in `PulseOpsConfig`:

- `PULSEOPS_CORS_ALLOWED_ORIGINS`
- `PULSEOPS_READ_ONLY_MODE`
- `PULSEOPS_SEED_CURATED_MONITORS`

## Data Model

### Monitor

Represents an API endpoint being monitored.

Fields:

- `id`
- `name`
- `url`
- `status`
- `last_status_code`
- `last_response_time_ms`
- `last_checked_at`
- `created_at`

Computed response fields:

- `totalChecks`
- `uptimePercentage`
- `averageResponseTimeMs`
- `fastestResponseTimeMs`
- `slowestResponseTimeMs`
- `latestErrorMessage`
- `latestFailureReason`
- `lastFailureAt`

Status values currently used:

- `UNKNOWN`
- `UP`
- `DOWN`

`DEGRADED` can be added later when an endpoint responds but is slow or unstable.

### CheckResult

Represents one health check result for one monitor.

Fields:

- `id`
- `monitor_id`
- `status`
- `status_code`
- `response_time_ms`
- `checked_at`
- `error_message`
- `created_at`

`status_code` can be null when the request fails before receiving an HTTP response.

### PublicApi

Represents a curated API option returned by `GET /api/public-apis`. These records are mapped from `CuratedMonitorDefinition` and can later power a frontend discovery flow.

Fields:

- `id`
- `name`
- `url`

## Relationships

```text
Monitor 1 -> many CheckResults
PublicApi can be copied into a Monitor later
```

## Planned Extensions

Future versions may add richer error-rate analytics, incident records, alerts, AI-generated summaries, and alternate backend implementations in Go or Python.
