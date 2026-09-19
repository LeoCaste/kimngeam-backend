-- Campos de auditoría y trazabilidad del corpus. Ninguno afecta la dimensión
-- de `embedding`, que sigue como VECTOR(1536) placeholder (ver V1 y CLAUDE.md).
ALTER TABLE corpus_chunk ADD COLUMN modelo_embedding TEXT;
ALTER TABLE corpus_chunk ADD COLUMN validado_por BIGINT REFERENCES usuario(id);
ALTER TABLE corpus_chunk ADD COLUMN validado_en TIMESTAMPTZ;
ALTER TABLE corpus_chunk ADD COLUMN activo BOOLEAN NOT NULL DEFAULT TRUE;
