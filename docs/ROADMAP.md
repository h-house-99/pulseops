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
- delete a monitor and its check history

The backend also includes a curated public API list endpoint. A frontend discovery flow can be added later.

## Current Limits

PulseOps does not include these yet:

- login or user accounts
- chart-ready history beyond the 5 most recent checks
- alerts or notifications
- public user submissions
- real top-searched API rankings
- production-grade security
- AI incident analysis

## Recommended Next

1. Add a chart-ready check history endpoint.
2. Add frontend latency and status charts for each monitor.
3. Add richer uptime and error-rate analytics.

The history endpoint should come before charts because the current frontend only fetches the 5 most recent checks. A useful first API shape would be `GET /api/monitors/{id}/checks?limit=50`, with a later `from` and `to` time range if needed.

## Next Features

- configurable scheduled check interval
- chart-ready check history
- latency trend charts
- uptime/status timeline charts
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
