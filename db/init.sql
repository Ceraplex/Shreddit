CREATE TABLE IF NOT EXISTS public.document_entity (
    id BIGSERIAL PRIMARY KEY,
    title TEXT,
    content TEXT,
    created_at TIMESTAMP NULL,
    username TEXT,
    filename TEXT,
    summary TEXT,
    summary_status TEXT,
    ocr_text TEXT,
    document_date DATE,
    tags TEXT
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

    -- Add document_date column if missing
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'document_entity'
          AND column_name = 'document_date'
    ) THEN
        EXECUTE 'ALTER TABLE public.document_entity ADD COLUMN document_date DATE';
    END IF;

    -- Add tags column if missing
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'document_entity'
          AND column_name = 'tags'
    ) THEN
        EXECUTE 'ALTER TABLE public.document_entity ADD COLUMN tags TEXT';
    END IF;
END $$;

-- Notes attached to documents
CREATE TABLE IF NOT EXISTS public.document_note (
    id BIGSERIAL PRIMARY KEY,
    document_id BIGINT NOT NULL REFERENCES public.document_entity(id) ON DELETE CASCADE,
    author TEXT,
    content TEXT NOT NULL,
    created_at TIMESTAMP NULL
);
CREATE INDEX IF NOT EXISTS idx_document_note_document_id ON public.document_note(document_id);

-- Users table for authentication
CREATE TABLE IF NOT EXISTS public.user_entity (
    id BIGSERIAL PRIMARY KEY,
    username TEXT UNIQUE NOT NULL,
    password_hash TEXT NOT NULL,
    created_at TIMESTAMP NULL
);

-- Comments on documents
CREATE TABLE IF NOT EXISTS public.comment (
    id BIGSERIAL PRIMARY KEY,
    document_id BIGINT NOT NULL REFERENCES public.document_entity(id) ON DELETE CASCADE,
    text TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_comment_document_id ON public.comment(document_id);
