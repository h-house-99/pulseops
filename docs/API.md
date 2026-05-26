# API

This is the lightweight API contract for the current PulseOps backend.

Base URL during local development:

```text
http://localhost:8080
```

## Local Database

The backend uses different databases depending on how it is run:

- Local app runtime uses Postgres.
- Automated tests use an in-memory H2 database.

Local runtime reads the JDBC URL from `backend/src/main/resources/application.properties` with a default of `jdbc:postgresql://localhost:5432/pulseops`. **Username and password are never committed:** set `SPRING_DATASOURCE_USERNAME` and `SPRING_DATASOURCE_PASSWORD` (optional URL override `SPRING_DATASOURCE_URL`). Copy `backend/.env.example` to `backend/.env`, fill in values, and load the file before starting the app (your shell or IDE should export those variables).

Example environment (placeholders only):

```bash
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/pulseops
export SPRING_DATASOURCE_USERNAME=your_postgres_username
export SPRING_DATASOURCE_PASSWORD=your_postgres_password
```

Committed defaults in `application.properties` also include `spring.jpa.hibernate.ddl-auto=update`.

Test config lives in `backend/src/test/resources/application.properties`:

```properties
spring.datasource.url=jdbc:h2:mem:pulseops_test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
spring.jpa.hibernate.ddl-auto=create-drop
```

In local development, Postgres should be running on `localhost:5432` and the `pulseops` database should exist. Hibernate creates or updates the `monitors` and `check_results` tables when the backend starts.

## Status Rules

Monitor checks use these rules:

- HTTP `200-399` -> `UP`
- HTTP `400+` -> `DOWN` with the returned status code
- timeout, DNS, connection, SSL, or other request failure -> `DOWN` with `statusCode: null` and an `errorMessage`

## Response Shapes

### Monitor

```json
{
  "id": 1,
  "name": "GitHub API",
  "url": "https://api.github.com",
  "status": "UNKNOWN",
  "lastStatusCode": null,
  "lastResponseTimeMs": null,
  "lastCheckedAt": null
}
```

Notes:

- `status` starts as `UNKNOWN`.
- `lastStatusCode`, `lastResponseTimeMs`, and `lastCheckedAt` are updated after `check-now`.
- `lastStatusCode` can be `null` when the request fails before receiving an HTTP response.

### Check Result

```json
{
  "id": 1,
  "monitorId": 1,
  "status": "UP",
  "statusCode": 200,
  "responseTimeMs": 143,
  "checkedAt": "2026-05-17T18:30:00Z",
  "errorMessage": null
}
```

Notes:

- `statusCode` can be `null` for timeout, DNS, connection, SSL, or similar failures.
- `errorMessage` is usually `null` when an HTTP response is received, even if the response is `500`.

## Health Check

Used to confirm that the frontend can talk to the backend.

```http
GET /api/health
```

Example response:

```json
{
  "status": "ok"
}
```

## List Public APIs

Returns a curated list of public APIs that can be added to monitoring.

```http
GET /api/public-apis
```

Example response:

```json
[
  {
    "id": 1,
    "name": "GitHub API",
    "description": "Public GitHub REST API",
    "url": "https://api.github.com",
    "category": "Developer Tools"
  }
]
```

## Create Monitor

Adds an API endpoint to the monitor list. Creating a monitor does not call the target URL yet.

```http
POST /api/monitors
```

Example request:

```json
{
  "name": "GitHub API",
  "url": "https://api.github.com"
}
```

Example response:

```json
{
  "id": 1,
  "name": "GitHub API",
  "url": "https://api.github.com",
  "status": "UNKNOWN",
  "lastStatusCode": null,
  "lastResponseTimeMs": null,
  "lastCheckedAt": null
}
```

## List Monitors

Returns all monitored API endpoints with their latest known check status.

```http
GET /api/monitors
```

Example response:

```json
[
  {
    "id": 1,
    "name": "GitHub API",
    "url": "https://api.github.com",
    "status": "UP",
    "lastStatusCode": 200,
    "lastResponseTimeMs": 143,
    "lastCheckedAt": "2026-05-17T18:30:00Z"
  }
]
```

## Check Monitor Now

Runs one manual health check for a monitored endpoint. This updates the monitor's latest status and stores a check result in history.

```http
POST /api/monitors/{id}/check-now
```

Example `UP` response:

```json
{
  "id": 10,
  "monitorId": 1,
  "status": "UP",
  "statusCode": 200,
  "responseTimeMs": 143,
  "checkedAt": "2026-05-17T18:30:00Z",
  "errorMessage": null
}
```

Example `DOWN` response from an HTTP error:

```json
{
  "id": 11,
  "monitorId": 1,
  "status": "DOWN",
  "statusCode": 500,
  "responseTimeMs": 98,
  "checkedAt": "2026-05-17T18:31:00Z",
  "errorMessage": null
}
```

Example `DOWN` response from a request failure:

```json
{
  "id": 12,
  "monitorId": 1,
  "status": "DOWN",
  "statusCode": null,
  "responseTimeMs": 3000,
  "checkedAt": "2026-05-17T18:32:00Z",
  "errorMessage": "Request timed out"
}
```

If the monitor does not exist, the backend returns `404`.

## List Monitor Checks

Returns 5 recent checks for one monitor.

```http
GET /api/monitors/{id}/checks/recent
```

Example response:

```json
[
  {
    "id": 10,
    "monitorId": 1,
    "status": "UP",
    "statusCode": 200,
    "responseTimeMs": 143,
    "checkedAt": "2026-05-17T18:30:00Z",
    "errorMessage": null
  }
]
```

If the monitor does not exist, the backend returns `404`.

## Investigate Monitor

Uses recent check history to generate an AI incident summary.

This endpoint is planned for a later version and is not implemented in v1 yet.

```http
POST /api/monitors/{id}/investigate
```

Example response:

```json
{
  "severity": "DEGRADED",
  "summary": "The endpoint is reachable, but response time increased during the last several checks.",
  "likelyCause": "The upstream API may be under load or rate limiting requests.",
  "recommendedActions": [
    "Check the provider status page.",
    "Increase client timeout temporarily.",
    "Continue monitoring for the next 10 minutes."
  ]
}
```
