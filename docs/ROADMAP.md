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
- see mapped failure reasons in plain language (for example, `Request timed out`, `HTTP 500`)
- map JDK `HttpTimeoutException` to `Request timed out` alongside `SocketTimeoutException`
- view latency charts for the last 1, 8, 24 hours, or 7 days
- show an hourly aggregated chart for the 7d window
- inspect chart points for exact latency, status, status code, and checked time
- see chart-window summary stats in expanded monitor cards
- cache chart history per monitor and time window in the frontend, with a 1-hour TTL for 7d data
- keep shorter chart windows live on dashboard refresh while avoiding redundant 7d refetches
- use a polished dashboard layout with clearer header copy, card spacing, and a wider shell on large screens
- delete a monitor and its check history
- clean up check results older than 30 days
- gate create, check, and delete UI actions behind `VITE_CAN_MANAGE_MONITORS`
- block backend create, manual check, and delete endpoints in read-only mode with `PULSEOPS_READ_ONLY_MODE`
- seed a curated monitor list on startup when `PULSEOPS_SEED_CURATED_MONITORS=true` and the database is empty
- serve the same curated catalog from `GET /api/public-apis`
- configure the frontend API base URL with `VITE_API_BASE_URL`
- configure backend CORS allowed origins with `PULSEOPS_CORS_ALLOWED_ORIGINS`
- deploy the read-only public demo to Render (static frontend, Docker backend, managed Postgres)
- keep the backend awake on Render free tier with an external `/api/health` cron ping
- document production deployment architecture in `docs/ARCHITECTURE.md`

Live demo: [https://pulseops-u82b.onrender.com/](https://pulseops-u82b.onrender.com/)

A frontend discovery flow can be added later.

## MVP Status

The read-only public demo is **live**. Visitors see curated monitors, scheduled checks, uptime summaries, failure reasons, and latency charts without create/check/delete controls.

Local development still supports the full Admin flow for learning and testing.

## Current Limits

PulseOps does not include these yet:

- login or user accounts
- alerts or notifications
- public user submissions
- real top-searched API rankings
- production-grade security
- AI incident analysis
- self-hosted deployment docs beyond the Render path

## Recommended Next

These are the highest-value next steps based on the current production deployment.

### Resume and demo polish

1. Add the live demo link and a fresh screenshot to `README.md`.
2. Update `docs/assets/pulseops-dashboard.jpg` to match the current UI.
3. Consider swapping the GitHub monitor from `api.github.com` to `githubstatus.com` to reduce cloud IP `403` noise in uptime stats.

### Reliability and observability

1. Decide whether intermittent `Request cancelled` / `Request failed` checks need more mapping or are acceptable noise on free-tier hosting.
2. Optionally make connect/read timeouts configurable instead of hard-coded 5 seconds.
3. Review whether unmapped endpoint failures should stay at `warn` level now that common timeouts are mapped.

### Product features (pick one theme next)

1. **Discovery flow** — wire `GET /api/public-apis` into the frontend so users can browse curated APIs.
2. **Uptime timeline** — status-over-time chart alongside latency.
3. **Incidents** — group consecutive `DOWN` checks into a simple incident record.
4. **Alerts** — email or webhook when a monitor flips to `DOWN`.

### Engineering cleanup

1. Clamp chart tooltips near left and right chart edges.
2. Move frontend API calls into a small API module and extract dashboard/chart hooks from `App.tsx`.
3. Add a combined history endpoint that returns checks plus summary stats for a time window.

## Near-Term Backlog

### Chart and analytics

- Clean up chart time-window fallback so missing fetch timestamps do not use `0`.
- Add backend aggregation or rate limiting before exposing large public chart fetches.

### Dashboard polish

- Recheck mobile chart spacing after desktop layout changes.

### Deployment and hosting

- Add optional self-hosting guide for always-on Linux hardware (HX310 path discussed in planning).
- Evaluate paid Render tier or migration off Render before free Postgres expiry becomes a blocker.

### Frontend code quality

- Refactor `App.tsx` after MVP data flows settle.
- Keep `App.tsx` mostly responsible for page layout and component wiring.

## Planning Timeline

Historical targets for context. Deployment landed in late June 2026.

| Target date | Work | Status |
| --- | --- | --- |
| June 10-14, 2026 | 7d charts, retention cleanup, failure reasons, chart caching, layout polish | Done |
| June 15-21, 2026 | Read-only MVP controls, seeding, env-based API URL/CORS | Done |
| June 22-30, 2026 | Deploy read-only demo, smoke test production monitors | Done |
| July 2026 | Demo polish, roadmap feature pick, optional self-hosting exploration | Next |

## Next Features

Candidate features to choose from for the next development cycle:

| Feature | Why it might be next | Effort |
| --- | --- | --- |
| README + screenshot refresh | Quick resume win; documents the live demo | Small |
| Swap GitHub to status page URL | Cleaner production uptime on Render | Small |
| Curated API discovery UI | Uses existing backend catalog; visible product feature | Medium |
| Uptime/status timeline chart | Complements latency charts; good demo story | Medium |
| Configurable check interval / timeout | Useful after seeing real production noise | Medium |
| Basic incident records | Natural follow-on once timeline exists | Medium–Large |
| Alerts / notifications | High product value; needs delivery channel choice | Large |
| Self-hosting on Linux | Removes Render sleep/DB expiry; more ops learning | Large |

## Later Ideas

- AI-generated incident summaries
- AI-suggested debugging steps
- provider status page lookup
- top monitored APIs
- API search tracking
- Go backend implementation
- Python FastAPI backend implementation
