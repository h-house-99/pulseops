# API

This document defines the first backend endpoints the React frontend will call.

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

Adds an API endpoint to the monitor list.

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

Returns all monitored API endpoints.

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
    "lastCheckedAt": "2026-05-08T17:30:00Z"
  }
]
```

## Check Monitor Now

Runs one manual health check for a monitored endpoint.

```http
POST /api/monitors/{id}/check-now
```

Example response:

```json
{
  "id": 10,
  "monitorId": 1,
  "status": "UP",
  "statusCode": 200,
  "responseTimeMs": 143,
  "checkedAt": "2026-05-08T17:30:00Z",
  "errorMessage": null
}
```

## List Monitor Checks

Returns recent check results for one endpoint.

```http
GET /api/monitors/{id}/checks
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
    "checkedAt": "2026-05-08T17:30:00Z",
    "errorMessage": null
  }
]
```

## Investigate Monitor

Uses recent check history to generate an AI incident summary.

This endpoint is planned for a later version.

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
