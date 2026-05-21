# PostgreSQL cheat sheet (PulseOps)

Local dev expects Postgres on `localhost:5432` with database `pulseops`. See `backend/src/main/resources/application.properties` and [API.md](./API.md) for credentials (`SPRING_DATASOURCE_*`).

**Prerequisites:** PostgreSQL installed (e.g. `brew install postgresql@16`).

| Task | Command | When to use |
|------|---------|-------------|
| Check if running | `pg_isready -h localhost -p 5432` | Quick health check (`accepting connections` = up). |
| Check (Homebrew) | `brew services list \| grep postgres` | See if the service is `started`. |
| Start server | `brew services start postgresql@16` | After install or reboot — run DB in background. |
| Stop server | `brew services stop postgresql@16` | Free port 5432 or shut down locally. |
| Restart server | `brew services restart postgresql@16` | After config changes or weird connection issues. |
| Connect to DB | `psql -h localhost -p 5432 -d pulseops` | Inspect tables, run SQL manually. |
| Create database | `createdb pulseops` | First-time setup if `pulseops` does not exist. |
| List databases | `psql -h localhost -p 5432 -l` | Confirm `pulseops` exists. |
| Who is using port 5432? | `lsof -i :5432` | Something else bound to 5432 — find the process. |

**Version note:** Replace `postgresql@16` with your installed version (`brew list \| grep postgres`).

**Tips**

- Spring Boot creates/updates tables on startup (Hibernate); you only need Postgres running and the `pulseops` DB.
- If `pg_isready` fails, start the service before `./mvnw spring-boot:run`.
- Backend: [MAVEN.md](./MAVEN.md) · Frontend: [VITE.md](./VITE.md)
