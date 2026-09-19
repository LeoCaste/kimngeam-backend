package com.kimngeam.backend.rag.generation;

/**
 * {@code modelo} guarda el identificador "proveedor:modelo" (ej.
 * {@code openrouter:google/gemini-3.8-flash}), el mismo formato que
 * {@code corpus_chunk.modelo_embedding} — se persiste tal cual en
 * {@code traduccion.modelo_llm} para la trazabilidad de qué generó la
 * traducción (ver CLAUDE.md).
 */
public record TranslationLlmResponse(String texto, String modelo) {
}
