package com.kimngeam.backend.rag.ingestion;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

/**
 * Shape exacto de una línea del JSONL de ingesta, tal como llega en el
 * archivo. Solo la usa {@link CorpusJsonlParser} para deserializar; el resto
 * del código trabaja con {@link com.kimngeam.backend.rag.ingestion.dto.CorpusChunkImportRecord}.
 */
record CorpusChunkJsonlLine(
		@JsonProperty("source_type") String sourceType,
		@JsonProperty("source_ref") String sourceRef,
		String contenido,
		String variante,
		Map<String, Object> metadata,
		boolean validado) {
}
