CREATE TABLE IF NOT EXISTS public.document_entity (
    id BIGSERIAL PRIMARY KEY,
    title TEXT,
    content TEXT,
    created_at TIMESTAMP NULL,
    username TEXT,
    filename TEXT
);

-- Users table for authentication
CREATE TABLE IF NOT EXISTS public.user_entity (
    id BIGSERIAL PRIMARY KEY,
    username TEXT UNIQUE NOT NULL,
    password_hash TEXT NOT NULL,
    created_at TIMESTAMP NULL
);
