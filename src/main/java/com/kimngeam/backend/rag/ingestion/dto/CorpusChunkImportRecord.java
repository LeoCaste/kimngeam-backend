package com.kimngeam.backend.rag.ingestion.dto;

import com.kimngeam.backend.shared.entity.SourceType;
import java.util.Map;

/**
 * Una línea ya parseada del JSONL de ingesta. {@code contenido} es el texto
 * bilingüe completo (líneas {@code MAP:} / {@code ESP:}) tal como se persiste
 * en {@code corpus_chunk.contenido}; no confundir con el texto que se embede,
 * que se deriva aparte (ver {@code CorpusEmbeddingTextExtractor}).
 */
public record CorpusChunkImportRecord(
		SourceType sourceType,
		String sourceRef,
		String contenido,
		String variante,
		Map<String, Object> metadata,
		boolean validado) {
}
