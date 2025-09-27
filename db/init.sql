-- Initializes the database schema for Shreddit (idempotent)
-- This script is executed automatically by the official postgres image
-- when mounted into /docker-entrypoint-initdb.d/ for a fresh database.

-- Create the table used by JPA entity com.fhtw.shreddit.model.DocumentEntity
-- Hibernate will also manage this table (ddl-auto=update). Keeping names
-- aligned avoids duplication and lets Hibernate add future columns.
CREATE TABLE IF NOT EXISTS public.document_entity (
    id BIGSERIAL PRIMARY KEY,
    title TEXT,
    content TEXT
);
