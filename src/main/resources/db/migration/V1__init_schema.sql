CREATE EXTENSION IF NOT EXISTS "pgcrypto";
CREATE EXTENSION IF NOT EXISTS "vector";

CREATE TABLE variante (
    nombre TEXT PRIMARY KEY
);

CREATE TABLE usuario (
    id              BIGSERIAL PRIMARY KEY,
    nombre          TEXT NOT NULL,
    apellido        TEXT,
    email           TEXT NOT NULL UNIQUE,
    password_hash   TEXT NOT NULL,
    inicial         CHAR(1) NOT NULL,
    rol             TEXT NOT NULL CHECK (rol IN ('academico', 'admin')),
    estado          TEXT NOT NULL DEFAULT 'activo' CHECK (estado IN ('activo', 'inactivo')),
    ultimo_acceso   TIMESTAMPTZ,
    creado_en       TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE invitacion (
    id           BIGSERIAL PRIMARY KEY,
    nombre       TEXT NOT NULL,
    email        TEXT NOT NULL UNIQUE,
    invitado_por BIGINT NOT NULL REFERENCES usuario(id),
    estado       TEXT NOT NULL DEFAULT 'pendiente' CHECK (estado IN ('pendiente', 'aceptada', 'expirada')),
    token        TEXT NOT NULL UNIQUE,
    creado_en    TIMESTAMPTZ NOT NULL DEFAULT now(),
    expira_en    TIMESTAMPTZ NOT NULL
);

CREATE TABLE token_invalidado (
    jti        TEXT PRIMARY KEY,
    usuario_id BIGINT NOT NULL REFERENCES usuario(id),
    expira_en  TIMESTAMPTZ NOT NULL
);

CREATE TABLE traduccion (
    id              TEXT PRIMARY KEY DEFAULT ('trad_' || replace(gen_random_uuid()::text, '-', '')),
    usuario_id      BIGINT REFERENCES usuario(id),
    texto_origen    TEXT NOT NULL,
    texto_traducido TEXT NOT NULL,
    direccion       TEXT NOT NULL CHECK (direccion IN ('es-map', 'map-es')),
    confianza       NUMERIC(3,2),
    fecha           TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_traduccion_usuario ON traduccion(usuario_id);

CREATE TABLE contexto_cultural (
    id            BIGSERIAL PRIMARY KEY,
    traduccion_id TEXT NOT NULL REFERENCES traduccion(id) ON DELETE CASCADE,
    expresion     TEXT NOT NULL,
    aporte        TEXT NOT NULL,
    comunidad     TEXT,
    variante      TEXT REFERENCES variante(nombre)
);

CREATE INDEX idx_contexto_traduccion ON contexto_cultural(traduccion_id);

CREATE TABLE validacion (
    id            TEXT PRIMARY KEY DEFAULT ('val_' || replace(gen_random_uuid()::text, '-', '')),
    usuario_id    BIGINT NOT NULL REFERENCES usuario(id),
    traduccion_id TEXT NOT NULL REFERENCES traduccion(id),
    expresion     TEXT,
    texto         TEXT NOT NULL,
    variante      TEXT REFERENCES variante(nombre),
    fecha         TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_validacion_usuario ON validacion(usuario_id);
CREATE INDEX idx_validacion_traduccion ON validacion(traduccion_id);

-- PENDIENTE: la dimension del vector (1536) es un placeholder alineado a
-- text-embedding-3-small / ada-002 de OpenAI. Ajustar segun el modelo de
-- embeddings que se elija (ej. 768 para muchos modelos open-source), lo que
-- requerira una migracion posterior que recree esta columna e indice.
CREATE TABLE corpus_chunk (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    source_type TEXT NOT NULL CHECK (source_type IN ('dictionary', 'translation_examples', 'expert_feedback')),
    source_ref  TEXT,
    contenido   TEXT NOT NULL,
    variante    TEXT REFERENCES variante(nombre),
    embedding   VECTOR(1536),
    metadata    JSONB,
    validado    BOOLEAN NOT NULL DEFAULT FALSE,
    creado_en   TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_corpus_embedding ON corpus_chunk USING hnsw (embedding vector_cosine_ops);
CREATE INDEX idx_corpus_source_type ON corpus_chunk(source_type);
