# Maven cheat sheet (PulseOps backend)

Run from `backend/` (uses the project wrapper `./mvnw`).

| Task | Command | When to use |
|------|---------|-------------|
| Compile | `./mvnw compile` | After code changes — quick check that it builds (no tests). |
| Run tests | `./mvnw test` | Before commit/PR — verify everything passes. |
| Run one test class | `./mvnw test -Dtest=MonitorApiIntegrationTest` | Debugging one test file — faster than full suite. |
| Run one test method | `./mvnw test -Dtest=MonitorApiIntegrationTest#postMonitorReturnsCreatedMonitor` | Fixing a single failing test. |
| Start the app | `./mvnw spring-boot:run` | Local dev — run the API on your machine (default port 8080). |
| Package JAR | `./mvnw package` | Build a runnable JAR for deploy or to run outside the IDE. |
| Clean build output | `./mvnw clean` | Weird build errors — delete `target/` and rebuild fresh. |
| Clean + compile | `./mvnw clean compile` | Fresh compile after dependency or config changes. |
| Clean + test | `./mvnw clean test` | Full clean run when tests behave oddly or caches look stale. |
| Package without tests | `./mvnw package -DskipTests` | Fast JAR build when you already ran tests separately. |

**Tips**

- Add `-q` for quieter output (e.g. `./mvnw -q test`).
- First run may download dependencies; needs network.
- On Windows use `mvnw.cmd` instead of `./mvnw`.
- For the React UI, see [VITE.md](./VITE.md).
- For PostgreSQL, see [POSTGRES.md](./POSTGRES.md).
