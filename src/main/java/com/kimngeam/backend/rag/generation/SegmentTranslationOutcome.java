package com.kimngeam.backend.rag.generation;

/**
 * Resultado de {@link SegmentTranslator#generar(String)}: el JSON ya parseado
 * más el identificador "proveedor:modelo" que lo generó (ver
 * {@link TranslationLlmResponse}), para persistir en {@code traduccion.modelo_llm}.
 */
public record SegmentTranslationOutcome(SegmentTranslationResult resultado, String modeloLlm) {
}
