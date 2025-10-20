# Shreddit

A simple document management app (Spring Boot + Postgres + NGINX static UI).

Quick start (Docker)
- Prerequisite: Install Docker Desktop (includes Docker Compose).
- From the repository root, start everything:
  - PowerShell/CMD: docker compose -f docker-compose.yml up -d --build
- Open the app:
  - UI: http://localhost/
  - API: http://localhost:8080/api
- Stop and remove containers (and volumes):
  - PowerShell/CMD: docker compose -f docker-compose.yml down -v

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


---

Backend tests and coverage
- We added JaCoCo to the backend (Maven) build with a 75% minimum instruction coverage check to demonstrate enforcement.
- To run tests and generate the coverage report locally:
  - Prerequisite: Have Apache Maven installed (or add the Maven Wrapper files). The provided wrapper scripts are present but the .mvn/wrapper directory is not committed.
  - From the repository root, run:
    - Windows PowerShell/CMD: mvn -f backend\pom.xml -DskipTests=false verify
    - macOS/Linux: mvn -f backend/pom.xml -DskipTests=false verify
  - Coverage report output: backend/target/site/jacoco/index.html

Notes on initial coverage rule
- The initial check is scoped to these classes to keep the build green while demonstrating the 75% requirement:
  - com.fhtw.shreddit.exception.*
  - com.fhtw.shreddit.security.JwtService
- You can broaden enforcement later by removing the <includes> filter in the JaCoCo check configuration inside backend/pom.xml (or by adding more packages).

Testing libraries used
- JUnit Jupiter (via spring-boot-starter-test)
- H2 is available for repository tests if desired in the future.
