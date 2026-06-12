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
- view latency charts for the last 1, 8, 24 hours, or 7 days
- show an hourly aggregated chart for the 7d window
- inspect chart points for exact latency, status, status code, and checked time
- see chart-window summary stats in expanded monitor cards
- delete a monitor and its check history
- clean up check results older than 30 days

The backend also includes a curated public API list endpoint. A frontend discovery flow can be added later.

## MVP Direction

The deployed MVP should be a read-only dashboard for a curated set of API health endpoints. Local development can keep create, delete, and manual check flows, but the public demo should focus on showing clean 7-day health and latency trends without allowing visitors to create scheduled monitors.

## Current Limits

PulseOps does not include these yet:

- login or user accounts
- alerts or notifications
- public user submissions
- real top-searched API rankings
- production-grade security
- AI incident analysis

## Recommended Next

1. Smooth chart loading states and tooltip edge behavior.
2. Normalize failure reasons before showing raw backend error text.
3. Prepare the public demo as read-only with curated monitors.

## Near-Term TODO

### Dashboard Layout

- Center monitor cards and forms within the main app shell.
- Let monitor cards and charts use more width on larger screens.
- Recheck mobile chart spacing after desktop layout changes.
- Prioritize UI polish before deployment prep.

### Chart And Analytics

- Keep expanded chart layout stable while new chart data is loading.
- Consider clamping chart tooltips near left and right chart edges.
- Normalize failure reasons before showing raw backend error text.

### Backend Data

- Consider a combined history endpoint that returns checks plus summary stats for a time window.
- Consider backend aggregation or caching for the 7d chart before public deployment.
- Disable public create, delete, and manual check actions for the deployed read-only demo.
- Seed the deployed demo with a curated monitor list.

## Planning Timeline

These dates are rough targets for keeping the MVP focused.

| Target date | Work | Estimate |
| --- | --- | --- |
| June 10-14, 2026 | Finish 7d chart support, retention cleanup, chart point details, docs, and UI polish | 1-2 sessions |
| June 15-21, 2026 | Add read-only MVP controls and final dashboard polish | 2-3 sessions |
| June 22-30, 2026 | Prepare read-only deployment config, curated monitors, environment variables, and production database setup | 2-4 sessions |
| July 1-7, 2026 | Deploy MVP, smoke test real monitors, and fix deployment issues | 2-3 sessions |

Anticipated MVP deployment target: **July 7, 2026**.

## Next Features

- configurable scheduled check interval
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
