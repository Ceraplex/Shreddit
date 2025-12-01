CREATE TABLE IF NOT EXISTS public.document_entity (
    id BIGSERIAL PRIMARY KEY,
    title TEXT,
    content TEXT,
    created_at TIMESTAMP NULL,
    username TEXT,
    filename TEXT,
    summary TEXT,
    summary_status TEXT,
    ocr_text TEXT
);

-- Ensure summary column is TEXT in case previous versions created it as VARCHAR(255)
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'document_entity'
          AND column_name = 'summary'
          AND data_type <> 'text'
    ) THEN
        EXECUTE 'ALTER TABLE public.document_entity ALTER COLUMN summary TYPE TEXT';
    END IF;

    -- Add ocr_text column as TEXT if missing
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'document_entity'
          AND column_name = 'ocr_text'
    ) THEN
        EXECUTE 'ALTER TABLE public.document_entity ADD COLUMN ocr_text TEXT';
    END IF;
END $$;

-- Users table for authentication
CREATE TABLE IF NOT EXISTS public.user_entity (
    id BIGSERIAL PRIMARY KEY,
    username TEXT UNIQUE NOT NULL,
    password_hash TEXT NOT NULL,
    created_at TIMESTAMP NULL
);
