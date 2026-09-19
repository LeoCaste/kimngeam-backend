package com.kimngeam.backend.rag.retrieval;

import com.kimngeam.backend.shared.entity.SourceType;
import java.util.UUID;

/**
 * Un chunk de {@code corpus_chunk} recuperado por {@link CorpusRetrievalService},
 * con su similitud coseno respecto a la consulta (1 = idéntico, 0 = sin
 * relación, según la distancia coseno de pgvector).
 */
public record RetrievedCorpusChunk(
		UUID id,
		SourceType sourceType,
		String sourceRef,
		String contenido,
		String variante,
		double similitud,
		boolean validado) {
}
