# Vite cheat sheet (PulseOps frontend)

Run from `frontend/` (uses npm).

**Prerequisites:** Node.js 20+ and npm.

| Task | Command | When to use |
|------|---------|-------------|
| Install dependencies | `npm install` | First clone, or after `package.json` changes. |
| Start dev server | `npm run dev` | Local dev — React with hot reload (default http://localhost:5173). |
| Production build | `npm run build` | Type-check and bundle into `dist/` for deploy. |
| Preview production build | `npm run preview` | Serve `dist/` locally to sanity-check the build. |
| Lint | `npm run lint` | Before commit/PR — run ESLint on the frontend. |

**Full stack (local)**

1. Backend: from `backend/`, run `./mvnw spring-boot:run` (API on port 8080). See [MAVEN.md](./MAVEN.md).
2. Frontend: from `frontend/`, run `npm run dev` (UI on port 5173).

**Tips**

- First `npm install` downloads packages; needs network.
- `npm run build` runs `tsc -b` then `vite build` — fix TypeScript errors before the bundle step succeeds.
- Output goes to `frontend/dist/` (ignored by git).
