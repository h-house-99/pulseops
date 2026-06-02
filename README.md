# PulseOps

PulseOps is a full-stack API monitoring dashboard built with a React frontend, a Spring Boot REST API, and PostgreSQL.

Version 1 is complete. It supports adding API monitors, running manual health checks, storing recent check history, deleting monitors, and viewing status, latency, uptime percentage, response-time summaries, latest errors, and last failure time from the frontend.

Version 2 will focus on scheduled checks, richer uptime analytics, incident records, alerts, and AI-assisted incident summaries.

## Screenshots

![PulseOps dashboard](docs/assets/pulseops-dashboard.jpg)

## Project Goal

Build a resume-ready app that demonstrates:

- REST API design
- HTTP client calls from the backend
- PostgreSQL persistence
- API health checks
- latency tracking, uptime summaries, and recent check history
- React dashboard UI
- clean separation between frontend and backend

## Current Demo Story

A user adds an API endpoint to the monitor list, runs a health check, and sees the latest status, status code, response time, uptime percentage, response-time summary statistics, latest error, last failure time, and recent check history.

## Tech Stack

- Frontend: React with Vite
- Backend: Java Spring Boot
- Database: PostgreSQL
- Tests: Spring Boot integration tests with H2

## Project Structure

```text
pulseops/
  backend/
  frontend/
  docs/
    API.md
    ARCHITECTURE.md
    ROADMAP.md
    SETUP.md
```

## Documentation

- [Setup](docs/SETUP.md)
- [API contract](docs/API.md)
- [Architecture](docs/ARCHITECTURE.md)
- [Roadmap](docs/ROADMAP.md)

## Version 1 Complete

Users can:

- add an API endpoint to the monitor list
- manually run a health check
- see status code, response time, and last checked time
- see uptime percentage and total checks
- see average, fastest, and slowest response times
- see the latest request error and last failure time
- view recent check history
- delete monitors and their check history

The backend also exposes a curated public APIs endpoint that can be connected to the frontend discovery flow later.

## Version 2 Ideas

- scheduled background checks
- richer uptime and error-rate analytics
- incident records
- alerts or notifications
- frontend discovery flow for curated public APIs
- AI-assisted incident summaries
- deployment
- alternate Go or Python backend implementations
