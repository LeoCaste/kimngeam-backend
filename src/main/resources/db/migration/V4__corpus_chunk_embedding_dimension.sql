-- Dimensión real del embedding: 1024, la que produce bge-m3 (baai/bge-m3),
-- el modelo elegido tras la prueba comparativa de Fase 2 con el corpus real
-- (reemplaza el placeholder VECTOR(1536) de la V1, alineado a OpenAI).
-- Si en el futuro se cambia de modelo, esta misma operación se repite:
-- dropear la columna, recrearla con la nueva dimensión y regenerar TODO el
-- corpus — los vectores no son comparables entre modelos ni dimensiones.
ALTER TABLE corpus_chunk DROP COLUMN embedding;
ALTER TABLE corpus_chunk ADD COLUMN embedding VECTOR(1024);
CREATE INDEX idx_corpus_embedding ON corpus_chunk USING hnsw (embedding vector_cosine_ops);
