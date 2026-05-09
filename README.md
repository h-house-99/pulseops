# PulseOps

A demo full-stack web app for monitoring public APIs and investigating incidents with AI.

The first version will use a React frontend, a Spring Boot backend, and PostgreSQL. The long-term goal is to keep the React UI stable while rebuilding the backend in other frameworks like Go and Python.

## Project Goal

Build a resume-ready app that demonstrates:

- REST API design
- HTTP client calls from the backend
- PostgreSQL persistence
- API health checks
- uptime and latency tracking
- React dashboard UI
- AI-assisted incident analysis
- clean separation between frontend and backend

## First Demo Story

A user chooses a public API to monitor, runs a health check, and sees the API status, response time, uptime history, and recent failures. When an endpoint looks unhealthy, the user can ask an AI incident assistant to summarize what happened and suggest next steps.

## Tech Stack

- Frontend: React with Vite
- Backend: Java Spring Boot
- Database: PostgreSQL
- AI: incident investigation assistant added after the core monitoring flow works

## Project Structure

```text
pulseops/
  backend/
  frontend/
  docs/
```

## Learning Rule

Build one small vertical slice at a time:

1. React talks to Spring Boot.
2. React displays a curated list of public APIs.
3. React sends a "check now" request to Spring Boot.
4. Spring Boot calls the public API and records the result.
5. Spring Boot saves check results to PostgreSQL.
6. React displays status, latency, and check history.
7. The AI assistant explains incidents using stored check data.

## Version 1 Scope

Users can:

- view a curated list of public APIs
- add an API endpoint to the monitor list
- manually run a health check
- see status code, response time, and last checked time
- view recent check history

Later versions can add scheduled checks, uptime percentages, incident records, AI summaries, deployment, and alternate Go/Python backends.
