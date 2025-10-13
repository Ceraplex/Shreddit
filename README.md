# Shreddit

A simple document management app (Spring Boot + Postgres + NGINX static UI).

Quick start (Docker)
- Prerequisite: Install Docker Desktop (includes Docker Compose).
- From the repository root, start everything:
  - PowerShell/CMD: docker compose -f backend/docker-compose.yml up -d --build
- Open the app:
  - UI: http://localhost/
  - API: http://localhost:8080/api
- Stop and remove containers (and volumes):
  - PowerShell/CMD: docker compose -f backend/docker-compose.yml down -v

What you get
- Backend (Spring Boot) on port 8080
- Database (PostgreSQL 16) on port 5432
- NGINX serving the static frontend on port 80 and proxying /api to the backend

Useful details
- Default database connection (for your local SQL client):
  - Host: localhost, Port: 5432, DB: shreddit, User: postgres, Password: postgres
- If a port is busy (80, 8080, or 5432), stop what’s using it or change the mapping in backend/docker-compose.yml.

Developers
- Run everything with the compose file under backend (the root compose file is deprecated).
- To rebuild after code changes in the backend Docker image:
  - docker compose -f backend/docker-compose.yml up -d --build

That’s it. Start the stack, open http://localhost/, and you’re ready to go.