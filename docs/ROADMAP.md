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

## MVP Status

The read-only public demo is **live**. Visitors see curated monitors, scheduled checks, uptime summaries, failure reasons, and latency charts without create/check/delete controls.

Local development still supports the full Admin flow for learning and testing.

The GitHub API monitor (`https://api.github.com`) is intentionally kept as-is. Intermittent `HTTP 403` responses from cloud IP rate limits are useful production noise and a good interview talking point.

## Current Limits

PulseOps does not include these yet:

- login or user accounts
- alerts or notifications
- public user submissions
- real top-searched API rankings
- production-grade security
- AI incident analysis
- self-hosted deployment docs beyond the Render path
- a dedicated status timeline separate from the latency chart
- P95 or other percentile stats in the monitor summary row (P95 is used internally for chart Y-axis scaling only)

## Recommended Next

Priority order based on production feedback and UI review (July 2026).

### 1. Expanded card UI polish (do first)

The expanded monitor card works but the summary row feels unfinished: stats pills hug the left, window context is unclear, and failure text sits as loose lines below the chart.

Planned improvements:

- Replace the left-aligned pill row with a full-width stat grid or metric tiles (label + value)
- Label stats with the selected chart window (for example, `Last 24h`) so window-scoped numbers feel intentional
- Show **P95** latency in the summary row; chart code already computes P95 for axis scaling
- Consider replacing **Fast** with P95, or tightening the row to `Uptime · Checks · Avg · P95 · Max`
- Combine last failure time and failure reason into one compact alert-style banner
- Update `docs/assets/pulseops-dashboard.jpg` after the pass

### 2. Status timeline strip (high-value monitoring feature)

The latency chart answers “how fast?” A **status timeline** answers “when was it down?”

Planned shape:

- Thin green/red segment bar under the latency chart, sharing the same time axis
- Each check becomes a visible UP/DOWN segment
- Makes blips like GitHub `HTTP 403` obvious without reading failure text

This is separate from the existing latency line chart, which already encodes status as dot color.

### 3. Overview / density improvements

On shorter viewports (for example Mac mini), only one expanded monitor fits comfortably because each card stacks window toggles, chart, stats, and failure info.

Planned options (pick one or combine):

- Collapsed cards show mini summary: status, window uptime, optional sparkline
- Optional accordion behavior: expanding one monitor collapses others
- Responsive chart height tweaks for medium-height viewports

### 4. Resume and demo polish

- Live demo link is in `README.md`; refresh screenshot after UI polish
- Optional per-monitor blurbs for curated APIs (for example, why GitHub may show `HTTP 403`)
- Optional outbound links to official vendor status pages

### 5. Product features (pick one theme after UI polish)

| Feature | What it is | Demo value |
| --- | --- | --- |
| **Curated API discovery UI** | Frontend browse/add flow wired to `GET /api/public-apis` | Higher in Admin/local mode; lower urgency for read-only demo |
| **Basic incidents** | Group consecutive `DOWN` checks into a simple incident record | Strong ops story; best after a status timeline exists |
| **Alerts** | Email or webhook on status flip | High value; needs delivery channel choice |
| **Configurable timeouts / check interval** | Env-driven connect/read timeout and cron interval | Useful after more production observation |

### 6. Reliability and observability (as needed)

- Decide whether intermittent `Request cancelled` checks are acceptable noise on free-tier hosting
- Review unmapped endpoint failure log levels now that `HttpTimeoutException` is mapped
- Deploy the timeout mapping fix to production if not already live

### 7. Engineering cleanup (when convenient)

- Clamp chart tooltips near left and right chart edges
- Move frontend API calls into a small API module and extract dashboard/chart hooks from `App.tsx`
- Add a combined history endpoint that returns checks plus summary stats for a time window
- Clean up chart time-window fallback so missing fetch timestamps do not use `0`

## Near-Term Backlog

### Chart and analytics

- Add backend aggregation or rate limiting before exposing large public chart fetches
- Expose P50/P99 or error-rate counts if the summary row still feels thin after P95

### Dashboard polish

- Recheck mobile and medium-height chart spacing after stat grid changes

### Deployment and hosting

- Add optional self-hosting guide for always-on Linux hardware (HX310 path discussed in planning)
- Evaluate paid Render tier or migration off Render before free Postgres expiry becomes a blocker

### Frontend code quality

- Refactor `App.tsx` after MVP data flows settle
- Keep `App.tsx` mostly responsible for page layout and component wiring

## Planning Timeline

| Target date | Work | Status |
| --- | --- | --- |
| June 10-14, 2026 | 7d charts, retention cleanup, failure reasons, chart caching, layout polish | Done |
| June 15-21, 2026 | Read-only MVP controls, seeding, env-based API URL/CORS | Done |
| June 22-30, 2026 | Deploy read-only demo, smoke test production monitors | Done |
| July 2026 (early) | Expanded card UI polish, P95 in summary, fresh screenshot | Next |
| July 2026 (mid) | Status timeline strip and/or overview card density | Planned |
| July 2026 (late) | Pick next product theme (discovery UI, incidents, or alerts) | Planned |

## Next Features

| Feature | Why it might be next | Effort |
| --- | --- | --- |
| Stat grid + window labels + failure banner | Fixes the “noobish” summary row; quick visual upgrade | Small |
| P95 in summary row | Reuses existing chart math; better ops metric than min/max | Small |
| Fresh dashboard screenshot | Resume/demo polish after UI pass | Small |
| Status timeline strip | Makes UP/DOWN history scannable; complements latency chart | Medium |
| Collapsed overview / accordion cards | Better use of vertical space on smaller screens | Medium |
| Curated API discovery UI | Browse/add from `GET /api/public-apis`; backend already exists | Medium |
| Per-monitor demo blurbs + status page links | Turns curated list into a learning demo | Small–Medium |
| Basic incident records | Follow-on after status timeline | Medium–Large |
| Configurable check interval / timeout | Production tuning | Medium |
| Alerts / notifications | Needs delivery channel choice | Large |
| Self-hosting on Linux | Removes Render sleep/DB expiry; more ops learning | Large |

## Deferred / Not Planned

- Swapping GitHub from `api.github.com` to `githubstatus.com` — kept for interview and production-rate-limit storytelling

## Later Ideas

- AI-generated incident summaries
- AI-suggested debugging steps
- provider status page lookup
- top monitored APIs
- API search tracking
- Go backend implementation
- Python FastAPI backend implementation
