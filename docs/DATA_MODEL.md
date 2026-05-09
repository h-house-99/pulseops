# Data Model

This document defines the first database tables for the Spring Boot backend.

## PublicApi

Represents a curated public API that the app can show in the discovery view.

Fields:

- `id`
- `name`
- `description`
- `url`
- `category`

Notes:

- These records can be seeded by the backend.
- Version 1 does not need users to submit public APIs.

## Monitor

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

Example `status` values:

- `UNKNOWN`
- `UP`
- `DOWN`
- `DEGRADED`

Notes:

- `UNKNOWN` means the endpoint has not been checked yet.
- `DEGRADED` can be added later when the endpoint responds but is slow or unstable.

## CheckResult

Represents one health check result for one monitor.

Fields:

- `id`
- `monitor_id`
- `status`
- `status_code`
- `response_time_ms`
- `checked_at`
- `error_message`

Notes:

- `status_code` can be null when the request fails before receiving a response.
- `error_message` stores timeout, DNS, connection, or unexpected request errors.

## IncidentSummary

Represents an AI-generated explanation for recent monitor behavior.

This table is planned for a later version.

Fields:

- `id`
- `monitor_id`
- `severity`
- `summary`
- `likely_cause`
- `recommended_actions`
- `created_at`

Notes:

- `recommended_actions` can be stored as JSON or a separate child table later.
- Version 1 can skip this table until AI investigation is added.

## Relationships

```text
PublicApi can be copied into a Monitor
Monitor 1 -> many CheckResults
Monitor 1 -> many IncidentSummaries
```
