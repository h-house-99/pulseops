# Architecture

PulseOps is a full-stack API monitoring dashboard with a React frontend, a Spring Boot backend, and PostgreSQL persistence.

## Components

```text
React + Vite frontend
  -> Spring Boot REST API
    -> PostgreSQL
    -> External API endpoints
```

The frontend manages the monitor dashboard, user input, loading states, monitor summary statistics, error details, and recent check history display.

The backend owns monitor validation, persistence, endpoint health checks, summary-stat calculation, and API response shaping.

PostgreSQL stores monitored endpoints and historical check results. Tests use H2 so backend integration tests can run without a local Postgres instance.

## Current Flow

1. A user adds a monitor from the React dashboard.
2. The frontend sends `POST /api/monitors`.
3. Spring Boot validates and stores the monitor with status `UNKNOWN`.
4. A user clicks check now.
5. The backend calls the monitor URL, records status code, latency, checked time, and any request error.
6. The backend recalculates monitor summary fields from stored check results.
7. The frontend refreshes the monitor list and displays status, uptime percentage, latency summaries, latest error details, and recent check history.

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

Represents a curated API option returned by the backend. These records are currently static backend responses and can later power a frontend discovery flow.

Fields:

- `id`
- `name`
- `description`
- `url`
- `category`

## Relationships

```text
Monitor 1 -> many CheckResults
PublicApi can be copied into a Monitor later
```

## Planned Extensions

Future versions may add scheduled checks, richer error-rate analytics, incident records, alerts, AI-generated summaries, and alternate backend implementations in Go or Python.
