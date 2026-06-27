# API

This is the lightweight API contract for the current PulseOps backend.

Base URL during local development:

```text
http://localhost:8080
```

## Read-Only Mode

PulseOps supports a backend read-only mode for the public demo. When enabled, user-triggered monitor management is blocked while read endpoints and scheduled checks keep working.

Configuration:

```properties
pulseops.read-only-mode=${PULSEOPS_READ_ONLY_MODE:false}
```

Set `PULSEOPS_READ_ONLY_MODE=true` in production or demo environments.

When read-only mode is enabled:

- `GET` endpoints continue to work normally.
- Scheduled background checks continue to run every 5 minutes.
- `POST /api/monitors`, `POST /api/monitors/{id}/check-now`, and `DELETE /api/monitors/{id}` return `403 Forbidden`.

Example `403` response body:

```json
{
  "status": 403,
  "error": "Forbidden",
  "message": "Monitor management is not allowed in read-only mode"
}
```

The frontend uses separate UI flags:

- `VITE_CAN_MANAGE_MONITORS` hides create, check, and delete controls.
- `VITE_API_BASE_URL` sets the backend API base URL, for example `http://localhost:8080/api`.

Backend read-only mode is the real enforcement layer.

## Curated Monitor Seeding

PulseOps can seed a curated monitor list into an empty database on startup. This is intended for demo and production deployments so the dashboard has monitors without manual `POST` requests.

Configuration:

```properties
pulseops.seed-curated-monitors=${PULSEOPS_SEED_CURATED_MONITORS:false}
```

Set `PULSEOPS_SEED_CURATED_MONITORS=true` when deploying to a fresh database.

When seeding is enabled:

- Seeding runs once at startup through `MonitorDataSeeder`.
- Monitors are inserted only when the `monitors` table is empty.
- Seeded monitors start with `status: "UNKNOWN"` until the scheduler or a manual check runs.
- Scheduled background checks continue to run normally after seeding.
- Seeding bypasses read-only mode because it is internal startup logic, not a public API call.

Current curated monitors:

- GitHub API — `https://api.github.com`
- OpenAI status — `https://status.openai.com/api/v2/status.json`
- Discord status — `https://discordstatus.com/api/v2/status.json`
- Cloudflare status — `https://www.cloudflarestatus.com/api/v2/status.json`
- Cat Facts — `https://catfact.ninja/fact`
- JSONPlaceholder — `https://jsonplaceholder.typicode.com/posts/1`
- HTTPBin 500 — `https://httpbin.org/status/500`

## Status Rules

Monitor checks use these rules:

- HTTP `200-399` -> `UP`
- HTTP `400+` -> `DOWN` with the returned status code
- timeout, DNS, connection, SSL, or other request failure -> `DOWN` with `statusCode: null` and an `errorMessage`

The backend uses a 5 second connect timeout and 5 second read timeout for outbound checks. JDK `HttpTimeoutException` and `SocketTimeoutException` both map to `errorMessage: "Request timed out"` and user-facing failure reason `Request timed out`.

## Response Shapes

### Monitor

All monitor-returning endpoints use this shape.

```json
{
  "id": 1,
  "name": "GitHub API",
  "url": "https://api.github.com",
  "status": "UP",
  "lastStatusCode": 200,
  "lastResponseTimeMs": 143,
  "lastCheckedAt": "2026-05-17T18:30:00Z",
  "totalChecks": 6,
  "uptimePercentage": 83,
  "averageResponseTimeMs": 184,
  "fastestResponseTimeMs": 91,
  "slowestResponseTimeMs": 721,
  "latestErrorMessage": "I/O error on GET request for https://api.github.com: Request cancelled",
  "latestFailureReason": "Request cancelled",
  "lastFailureAt": "2026-05-17T18:32:00Z"
}
```

Notes:

- `status` starts as `UNKNOWN`.
- `lastStatusCode`, `lastResponseTimeMs`, and `lastCheckedAt` are updated after `check-now`.
- `lastStatusCode` can be `null` when the request fails before receiving an HTTP response.
- Newly created monitors have `status: "UNKNOWN"` and no last check values yet.
- `totalChecks` is the number of stored check results for the monitor. New Monitors have `totalChecks: 0`.
- `uptimePercentage` is the percentage of stored checks with status `UP`. It is `null` when no checks exist.
- `averageResponseTimeMs`, `fastestResponseTimeMs`, and `slowestResponseTimeMs` are calculated from stored check results.
- Summary latency fields are `null` when no checks exist.
- `latestErrorMessage` is the most recent non-null request error message, or `null`.
- `latestFailureReason` is the most recent `DOWN` check reason in simple language, or `null`.
- `lastFailureAt` is the most recent `DOWN` check time, or `null`.

### Check Result

Check-running and check-history endpoints use this shape.

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

- `status` is `UP` for HTTP `200-399`; otherwise it is `DOWN`.
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

Returns the curated public API catalog. This list is sourced from the same definitions used for demo monitor seeding and can later power a frontend discovery flow.

```http
GET /api/public-apis
```

Example response:

```json
[
  {
    "id": 1,
    "name": "GitHub API",
    "url": "https://api.github.com"
  },
  {
    "id": 2,
    "name": "OpenAI status",
    "url": "https://status.openai.com/api/v2/status.json"
  }
]
```

Notes:

- `id` is a stable catalog index, not a monitor database id.
- The response currently includes seven curated APIs.
- `description` and `category` are not included yet and can be added later if the discovery UI needs them.

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

Returns a `Monitor` response.

Errors:

- If the request is invalid, the backend returns `400`.
- If read-only mode is enabled, the backend returns `403`.

## List Monitors

Returns all monitored API endpoints with their latest known check status.

```http
GET /api/monitors
```

Returns an array of `Monitor` responses.

## Check Monitor Now

Runs one manual health check for a monitored endpoint. This updates the monitor's latest status and stores a check result in history.

```http
POST /api/monitors/{id}/check-now
```

Returns a `Check Result` response.

Example `UP` result:

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

Example `DOWN` result from an HTTP error:

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

Example `DOWN` result from a request failure:

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

Errors:

- If read-only mode is enabled, the backend returns `403`.
- If the monitor does not exist, the backend returns `404`.

## Scheduled Checks

The backend also runs scheduled checks for all monitors every 5 minutes. Scheduled checks use the same status rules and stored `Check Result` shape as manual checks, but they do not have a separate public API endpoint.

## List Monitor Checks

Returns 5 recent checks for one monitor.

```http
GET /api/monitors/{id}/checks/recent
```

Returns an array of `Check Result` responses.

Errors:

- If the monitor does not exist, the backend returns `404`.

## List Monitor Checks By Time Window

Returns check results for one monitor within the last number of hours. This endpoint is intended for chart rendering.

```http
GET /api/monitors/{id}/checks
GET /api/monitors/{id}/checks?hours=24
```

If `hours` is omitted, the backend defaults to `24`.

Allowed `hours` values:

- `1`
- `8`
- `24`
- `168` (7 days)

Results are ordered by `checkedAt` ascending, oldest to newest.

Returns an array of `Check Result` responses.

Errors:

- If the monitor does not exist, the backend returns `404`.
- If `hours` is unsupported, the backend returns `400`.

## Delete Monitor

Deletes a monitored endpoint and its stored check results.

```http
DELETE /api/monitors/{id}
```

Successful responses return `204 No Content`.

Errors:

- If read-only mode is enabled, the backend returns `403`.
- If the monitor does not exist, the backend returns `404`.

