-- Enable UUID generation
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- Main documents table
CREATE TABLE documents (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

  filename TEXT NOT NULL,
  content_type TEXT NOT NULL,

  status TEXT NOT NULL DEFAULT 'UPLOADED',

  extracted_text TEXT,
  retry_count INTEGER NOT NULL DEFAULT 0,
  error_message TEXT,

  uploaded_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
  processing_started_at TIMESTAMP WITH TIME ZONE,
  completed_at TIMESTAMP WITH TIME ZONE
);