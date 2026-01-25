-- Create the comment table for storing comments on documents
CREATE TABLE IF NOT EXISTS public.comment (
    id BIGSERIAL PRIMARY KEY,
    document_id BIGINT NOT NULL REFERENCES public.document_entity(id) ON DELETE CASCADE,
    text TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create an index on document_id for faster lookups
CREATE INDEX IF NOT EXISTS idx_comment_document_id ON public.comment(document_id);

-- Add a comment explaining the table
COMMENT ON TABLE public.comment IS 'Stores comments/notes attached to documents';
COMMENT ON COLUMN public.comment.document_id IS 'Reference to the document this comment belongs to';
COMMENT ON COLUMN public.comment.text IS 'The text content of the comment';
COMMENT ON COLUMN public.comment.created_at IS 'Timestamp when the comment was created';