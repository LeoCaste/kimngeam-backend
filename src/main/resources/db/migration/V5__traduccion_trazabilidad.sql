-- Trazabilidad completa de cada traducción: de qué segmentos se compone, qué
-- chunks del corpus respaldaron cada uno (con qué score) y qué modelo la
-- generó. Es el punto central del proyecto (ver CLAUDE.md), no un extra.
ALTER TABLE traduccion ADD COLUMN modelo_llm TEXT;

CREATE TABLE traduccion_segmento (
    id              BIGSERIAL PRIMARY KEY,
    traduccion_id   TEXT NOT NULL REFERENCES traduccion(id) ON DELETE CASCADE,
    orden           INT NOT NULL,
    texto_origen    TEXT NOT NULL,
    texto_traducido TEXT NOT NULL,
    confianza       NUMERIC(3,2),
    con_respaldo    BOOLEAN NOT NULL DEFAULT FALSE
);
CREATE INDEX idx_segmento_traduccion ON traduccion_segmento(traduccion_id);

CREATE TABLE traduccion_fuente (
    id              BIGSERIAL PRIMARY KEY,
    segmento_id     BIGINT NOT NULL REFERENCES traduccion_segmento(id) ON DELETE CASCADE,
    corpus_chunk_id UUID NOT NULL REFERENCES corpus_chunk(id),
    score           NUMERIC(5,4) NOT NULL
);
CREATE INDEX idx_fuente_segmento ON traduccion_fuente(segmento_id);
