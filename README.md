# Shreddit
SWEN 3 Document Management System

## Sprint 2: Web UI (React + nginx + CORS)

This repo includes a minimal Web-UI served by NGINX which proxies API requests to the Spring Boot backend. The backend service is NOT exposed directly to the host; only NGINX listens on port 80.

- Frontend: Static React SPA (served by NGINX at http://localhost/)
- Backend: Spring Boot 3 (internal at app:8080), generated controllers from OpenAPI
- DB: PostgreSQL 16 (optionally accessible at localhost:5432)
- Reverse proxy: NGINX (proxies /api to backend)
- CORS: enabled in Spring for common dev origins; NGINX also handles OPTIONS in /api
- Data persistence: Postgres uses a named volume `db-data`

### Run
1. Build the images
   - `docker compose build`
2. Start the stack
   - `docker compose up -d`
3. Open the UI
   - http://localhost/

You should see the Shreddit Frontend. It can:
- List documents (GET /api/documents)
- Create a document (POST /api/documents)
- Delete a document (DELETE /api/documents/{id})

NGINX forwards `/api/*` to the backend container, so the browser sees a single origin (no CORS needed at runtime). For development from another port, Spring’s global CORS config allows localhost ports 3000, 4200, and 5173 by default (see CorsConfig).

### Troubleshooting
- Web server failed to start. Port 8080 was already in use.
  - This happens when you run the backend locally and something else already uses 8080 (another Spring app, Docker Desktop, etc.). Fixes:
    - Free the port (Windows PowerShell): `Get-Process -Id (Get-NetTCPConnection -LocalPort 8080).OwningProcess | Stop-Process`
    - Or run on another port: set env var before starting: `set SERVER_PORT=8081` (Windows) or `export SERVER_PORT=8081` (macOS/Linux).
    - In Docker Compose there is no host port conflict because the backend port is not published.
- Can’t see the database / driver errors
  - The stack uses Postgres 16. The DB is exposed to your host on port 5432. Connect using:
    - Host: `localhost`, Port: `5432`, DB: `shreddit`, User: `postgres`, Password: `postgres`.
  - If the backend starts before Postgres is ready you might see connection/driver-like errors. Compose now waits for Postgres health before starting the app.
  - Optionally use pgAdmin or any SQL client to inspect the DB.

### Notes
- JWT ready: the frontend includes a tiny fetch wrapper that attaches a `Bearer` token from `localStorage.jwt` when present.
- DTOs: The backend uses OpenAPI-generated models; an `Entity` extends the API model and a service maps between DTO and JPA.
- OpenAPI: Edit `backend/src/main/resources/openapi.yaml` and the controllers will regenerate during Maven build.

### Compose services
- `app` – Spring Boot backend (internal only at 8080)
- `db` – PostgreSQL 16 with named volume `db-data`
- `nginx` – Serves the React SPA and proxies `/api` to `app:8080`

### CORS and Dev Proxies
When the frontend and backend run on different ports (typical during dev), browsers enforce CORS. You have these options:

- Spring Boot (@CrossOrigin / global config): This repo enables global CORS in `CorsConfig` for common dev ports (3000, 4200, 5173) on localhost and 127.0.0.1.
- ASP.NET Core (if applicable): Add CORS middleware and allow your dev origin.
- Dev proxy (recommended): Configure your dev server to proxy `/api` to the backend so the browser sees one origin.
  - Angular: angular.json proxy or proxy.conf.json → target `http://localhost:8080`
  - Vite: vite.config.js → `server: { proxy: { '/api': 'http://localhost:8080' } }`
  - CRA: setupProxy.js → proxy `/api` to `http://localhost:8080`

Because NGINX serves the UI at `http://localhost/` and proxies `/api` to the backend in Docker, no CORS is needed at runtime in the compose setup.

## FAQ: Do I need Node.js or npm? Are we using React?
- Yes, we are using React. The UI at `frontend/build/index.html` loads React 18 and ReactDOM from a CDN (UMD builds) and uses `babel-standalone` in the browser to transpile the JSX in-place.
- You do NOT need Node.js or npm to run the app with Docker Compose. NGINX serves the static files and proxies `/api` to the backend.
- When would you need npm? Only if you want a local developer experience with a modern toolchain (e.g., Vite/CRA, TypeScript, unit tests, hot reload). That is optional.

Optional dev setup with Vite (if you want it):
1. Create a React app (outside of Docker):
   - `npm create vite@latest shreddit-frontend -- --template react`
   - `cd shreddit-frontend && npm install`
2. Configure Vite dev proxy to your backend so calls to `/api` work without CORS (vite.config.js):
   - `export default { server: { proxy: { '/api': 'http://localhost:8080' } } }`
3. Develop with hot reload:
   - `npm run dev` (usually serves at http://localhost:5173)
4. Build and deploy into this repo’s static folder when you’re ready:
   - `npm run build` → copy the build output into `frontend/build/` so NGINX serves it.

Notes:
- Because NGINX serves the UI at `http://localhost/` and proxies `/api` to the backend, the browser sees a single origin. No CORS is needed in production. For dev on another port (e.g., Vite at 5173), Spring’s global CORS allows localhost ports 3000/4200/5173 and 127.0.0.1 variants.
- The current static setup already supports quick edits: save `frontend/build/index.html` and refresh the page. No build step required.