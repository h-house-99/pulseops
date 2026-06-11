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
- inspect chart points for exact latency, status, status code, and checked time
- delete a monitor and its check history

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

1. Improve dashboard layout for wider screens.
2. Make expanded chart analytics reflect the selected time window.
3. Add backend retention cleanup for old check results.

## Near-Term TODO

### Dashboard Layout

- Center monitor cards and forms within the main app shell.
- Let monitor cards and charts use more width on larger screens.
- Recheck mobile chart spacing after desktop layout changes.
- Prioritize UI polish before deployment prep.

### Chart And Analytics

- Add a 7d chart window and make it the default expanded view.
- Move uptime, check count, and latency summary stats into the expanded chart section.
- Make expanded summary stats reflect the selected time window.
- Remove the recent checks table if chart tooltips cover the same detail.
- Consider clamping chart tooltips near left and right chart edges.

### Backend Data

- Add retention cleanup for check results older than 7 days.
- Consider a combined history endpoint that returns checks plus summary stats for a time window.
- Disable public create, delete, and manual check actions for the deployed read-only demo.
- Seed the deployed demo with a curated monitor list.

## Planning Timeline

These dates are rough targets for keeping the MVP focused.

| Target date | Work | Estimate |
| --- | --- | --- |
| June 10-14, 2026 | Finish chart window selector, chart point details, docs, and final cleanup | 1-2 sessions |
| June 15-21, 2026 | Add 7d analytics, selected-window summary stats, and layout polish | 2-3 sessions |
| June 22-30, 2026 | Prepare read-only deployment config, curated monitors, environment variables, and production database setup | 2-4 sessions |
| July 1-7, 2026 | Deploy MVP, smoke test real monitors, and fix deployment issues | 2-3 sessions |

Anticipated MVP deployment target: **July 7, 2026**.

## Next Features

- configurable scheduled check interval
- uptime/status timeline charts
- 7d chart window
- selected-window analytics summaries
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
