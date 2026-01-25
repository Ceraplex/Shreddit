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

open http://localhost/ and if you want, check minIO: http://localhost:9001/
and RabbitMQ: http://localhost:15672/

---

Backend tests and coverage

- added JaCoCo to the backend (Maven) build with a 75% minimum instruction coverage check to demonstrate enforcement.
- To run tests and generate the coverage report locally:
  - Prerequisite: Have Apache Maven installed (or add the Maven Wrapper files). The provided wrapper scripts are present but the .mvn/wrapper directory is not committed.
  - From the repository root, run:
    - Windows PowerShell/CMD: mvn -f backend\pom.xml -DskipTests=false verify
    - macOS/Linux: mvn -f backend/pom.xml -DskipTests=false verify
  - Coverage report output: backend/target/site/jacoco/index.html

Integration test HOWTO (required)

- The provided integration tests are executed via the JUnit suite `ShredditApplicationTests`.
- Run the full backend test suite (unit + integration) from the repo root:
  - Windows PowerShell/CMD: `mvn -f backend\pom.xml -Dtest=ShredditApplicationTests test`
  - macOS/Linux: `mvn -f backend/pom.xml -Dtest=ShredditApplicationTests test`
- This suite scans all tests under `com.fhtw.shreddit.*` and should finish successfully end-to-end.
- Tests use the H2 in-memory database (`backend/src/test/resources/application-test.properties`), so Docker services are not required for the suite.

XML import worker tests (separate module)

- Run its unit + end-to-end tests:
  - Windows PowerShell/CMD: `mvn -f xml-import-worker\pom.xml test`
  - macOS/Linux: `mvn -f xml-import-worker/pom.xml test`

Notes on initial coverage rule

- The initial check is scoped to these classes to keep the build green while demonstrating the 75% requirement:
  - com.fhtw.shreddit.exception.\*
  - com.fhtw.shreddit.security.JwtService

Testing libraries used

- JUnit Jupiter (via spring-boot-starter-test)
- H2 is available for repository tests

XML document import (document replacement)

- The `xml-import-worker` reads XML files from a configurable folder and writes them directly to Postgres.
- Runs daily at 01:00 via `XMLIMPORT_CRON` (default `0 0 1 * * *`).
- Input folder and filename pattern are configurable via `XMLIMPORT_INPUT_DIR` and `XMLIMPORT_FILE_PATTERN`.
- Processed XML files are moved to the archive folder (`XMLIMPORT_ARCHIVE_DIR`) to avoid duplicates.

XML format (single document per file)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<document>
    <filename>contract-2026-01-19.pdf</filename>
    <title>Contract Review</title>
    <datum>2026-01-19</datum>
    <tags>
        <tag>vertrag</tag>
        <tag>legal</tag>
    </tags>
    <username>xml-import</username>
    <summary>Manuell importiertes Dokument aus einer XML-Datei.</summary>
    <content>Dies ist ein Beispieltext, der als Dokumentinhalt gespeichert wird.</content>
</document>
```

Sample file

- `xml-import/inbox/document-2026-01-19.xml`

## Document Comments

The application supports adding comments to documents. Comments are stored in a separate table and are associated with a document via a foreign key.

### Database Schema

The comment table is automatically created by Hibernate when the application starts, based on the CommentEntity class. If you need to create the table manually, you can use the SQL script in `db/comment.sql`:

```sql
CREATE TABLE IF NOT EXISTS public.comment (
    id BIGSERIAL PRIMARY KEY,
    document_id BIGINT NOT NULL REFERENCES public.document_entity(id) ON DELETE CASCADE,
    text TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_comment_document_id ON public.comment(document_id);
```

### API Endpoints

#### Get Comments for a Document

```
GET /documents/{documentId}/comments
```

Returns a list of comments for the specified document, sorted by creation date (newest first).

Example:

```bash
curl -X GET http://localhost:8080/documents/1/comments
```

Response:

```json
[
  {
    "id": 2,
    "documentId": 1,
    "text": "This is a newer comment",
    "createdAt": "2026-01-25T15:30:45.123"
  },
  {
    "id": 1,
    "documentId": 1,
    "text": "This is an older comment",
    "createdAt": "2026-01-25T14:20:30.456"
  }
]
```

#### Add a Comment to a Document

```
POST /documents/{documentId}/comments
```

Adds a new comment to the specified document.

Example:

```bash
curl -X POST http://localhost:8080/documents/1/comments \
  -H "Content-Type: application/json" \
  -d '{"text": "This is a new comment"}'
```

Response:

```json
{
  "id": 3,
  "documentId": 1,
  "text": "This is a new comment",
  "createdAt": "2026-01-25T16:45:12.789"
}
```

#### Delete a Comment

```
DELETE /comments/{commentId}
```

Deletes the specified comment.

Example:

```bash
curl -X DELETE http://localhost:8080/comments/3
```

Response: 204 No Content
