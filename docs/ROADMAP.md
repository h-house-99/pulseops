# Roadmap

This document separates what PulseOps supports today from planned future work.

## Completed

PulseOps currently supports:

- add a custom API endpoint to the monitor list
- manually run a health check
- run scheduled background checks every 5 minutes
- see whether the endpoint is up or down
- see status code, response time, and last checked time
- see total checks and uptime percentage
- see average, fastest, and slowest response times
- see the latest request error and last failure time
- view recent check history for an endpoint
- view latency charts for the last 1, 8, or 24 hours
- delete a monitor and its check history

The backend also includes a curated public API list endpoint. A frontend discovery flow can be added later.

## Current Limits

PulseOps does not include these yet:

- login or user accounts
- alerts or notifications
- public user submissions
- real top-searched API rankings
- production-grade security
- AI incident analysis

## Recommended Next

1. Improve chart UX with point details for exact latency, status, and checked time.
2. Add richer uptime and error-rate analytics.
3. Add basic incident records for downtime periods.

## Planning Timeline

These dates are rough targets for keeping the MVP focused.

| Target date | Work | Estimate |
| --- | --- | --- |
| June 10-14, 2026 | Finish chart window selector, docs, and final cleanup | 1-2 sessions |
| June 15-21, 2026 | Add chart point details/tooltips and polish loading/error states | 2-3 sessions |
| June 22-30, 2026 | Prepare deployment config, environment variables, and production database setup | 2-4 sessions |
| July 1-7, 2026 | Deploy MVP, smoke test real monitors, and fix deployment issues | 2-3 sessions |

Anticipated MVP deployment target: **July 7, 2026**.

## Next Features

- configurable scheduled check interval
- uptime/status timeline charts
- chart point details or tooltips
- error rate summaries
- basic incident records
- alerts or notifications
- frontend discovery flow for curated public APIs
- deployment

## Later Ideas

- AI-generated incident summaries
- AI-suggested debugging steps
- provider status page lookup
- top monitored APIs
- API search tracking
- Go backend implementation
- Python FastAPI backend implementation
