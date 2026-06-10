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
