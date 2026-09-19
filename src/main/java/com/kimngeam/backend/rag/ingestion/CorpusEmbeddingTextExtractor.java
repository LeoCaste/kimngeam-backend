package com.kimngeam.backend.rag.ingestion;

import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/**
 * El texto que se embede NO es el mismo que se guarda en
 * {@code corpus_chunk.contenido}: se guarda el bilingüe completo (es lo que
 * ve el LLM en generación), pero el embedding se genera solo con las líneas
 * {@code ESP:}. Indexar bilingüe da peores resultados de recuperación que
 * indexar solo el español — validado empíricamente, ver CLAUDE.md.
 */
@Component
public class CorpusEmbeddingTextExtractor {

	private static final String PREFIJO_ESPANOL = "ESP:";

	public String extraerTextoParaEmbedding(String contenidoBilingue) {
		return contenidoBilingue.lines()
				.map(String::strip)
				.filter(linea -> linea.startsWith(PREFIJO_ESPANOL))
				.map(linea -> linea.substring(PREFIJO_ESPANOL.length()).strip())
				.collect(Collectors.joining("\n"));
	}
}
