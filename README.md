# PulseOps

PulseOps is an in-progress full-stack web app for monitoring API endpoints from a React dashboard backed by a Spring Boot REST API.

The current version supports adding API monitors, running manual health checks, storing recent check history, and viewing status/latency from the frontend. Planned later versions can add scheduled checks, uptime analytics, incident records, and AI-assisted incident summaries.

## Screenshots

![PulseOps dashboard](docs/assets/pulseops-dashboard.jpg)

## Project Goal

Build a resume-ready app that demonstrates:

- REST API design
- HTTP client calls from the backend
- PostgreSQL persistence
- API health checks
- latency tracking and recent check history
- React dashboard UI
- clean separation between frontend and backend

## Current Demo Story

A user adds an API endpoint to the monitor list, runs a health check, and sees the latest status, status code, response time, and recent check history.

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

## Version 1 Scope

Users can:

- add an API endpoint to the monitor list
- manually run a health check
- see status code, response time, and last checked time
- view recent check history
- delete monitors and their check history

The backend also exposes a curated public APIs endpoint that can be connected to the frontend discovery flow later.

Later versions can add scheduled checks, uptime percentages, incident records, AI summaries, deployment, and alternate Go/Python backends.
