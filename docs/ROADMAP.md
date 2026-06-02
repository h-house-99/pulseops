# Roadmap

This document separates what PulseOps supports today from planned future work.

## Version 1 - Complete

Users can:

- add a custom API endpoint to the monitor list
- manually run a health check
- see whether the endpoint is up or down
- see status code, response time, and last checked time
- see total checks and uptime percentage
- see average, fastest, and slowest response times
- see the latest request error and last failure time
- view recent check history for an endpoint
- delete a monitor and its check history

The backend also includes a curated public API list endpoint. A frontend discovery flow can be added later.

## Not In Version 1

The first version does not include:

- login or user accounts
- scheduled background checks
- alerts or notifications
- public user submissions
- real top-searched API rankings
- production-grade security
- AI incident analysis

## Version 2

Possible next features:

- scheduled checks
- richer uptime and error-rate analytics
- error rate
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
