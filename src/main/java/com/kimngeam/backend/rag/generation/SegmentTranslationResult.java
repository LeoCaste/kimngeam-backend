package com.kimngeam.backend.rag.generation;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Shape del JSON que el LLM debe devolver por segmento (ver la plantilla en
 * {@code prompts/segmento-traduccion.txt} y {@link TranslationResponseParser}).
 * Nunca incluye una confianza: eso se deriva del retrieval, jamás se le pide
 * al modelo (ver CLAUDE.md).
 */
public record SegmentTranslationResult(String traduccion,
		@JsonProperty("contexto_cultural") List<ContextoCulturalGenerado> contextoCultural) {

	public SegmentTranslationResult {
		contextoCultural = contextoCultural == null ? List.of() : List.copyOf(contextoCultural);
	}
}
