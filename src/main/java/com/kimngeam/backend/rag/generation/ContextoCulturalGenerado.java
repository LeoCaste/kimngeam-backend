package com.kimngeam.backend.rag.generation;

/**
 * Un aporte cultural tal como lo devuelve el LLM en el JSON de un segmento
 * (ver {@link SegmentTranslationResult}) — {@code comunidad} y
 * {@code variante} pueden venir {@code null} cuando el modelo no las conoce
 * con certeza.
 */
public record ContextoCulturalGenerado(String expresion, String aporte, String comunidad, String variante) {
}
