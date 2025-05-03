-- V1__Create_vector_extension.sql
-- Enable the vector extension required for embeddings.
-- The 'IF NOT EXISTS' ensures this doesn't fail if run again
-- or if the extension was manually created.
CREATE EXTENSION IF NOT EXISTS vector;