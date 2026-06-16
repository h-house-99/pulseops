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

## Read-Only Demo Mode

PulseOps uses two environment flags for the public demo:

| Layer | Flag | Purpose |
| --- | --- | --- |
| Frontend | `VITE_CAN_MANAGE_MONITORS` | Hides create, check, and delete UI actions |
| Backend | `PULSEOPS_READ_ONLY_MODE` | Blocks write API calls with `403 Forbidden` |

Read-only enforcement lives in `MonitorService`. User-triggered write methods call `ensureMonitorManagementAllowed()` before creating monitors, running manual checks, or deleting monitors.

When read-only mode is enabled:

- `POST /api/monitors`, `POST /api/monitors/{id}/check-now`, and `DELETE /api/monitors/{id}` are rejected.
- `GET /api/monitors`, chart history endpoints, health checks, and scheduled background checks continue to work.

Local development usually keeps backend read-only off and frontend management UI on. The deployed demo should set `PULSEOPS_READ_ONLY_MODE=true` and `VITE_CAN_MANAGE_MONITORS=false`.

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

Demo deployment flags are managed together in `PulseOpsConfig`:

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
