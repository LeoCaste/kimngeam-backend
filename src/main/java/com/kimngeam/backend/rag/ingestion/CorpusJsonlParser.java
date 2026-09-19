package com.kimngeam.backend.rag.ingestion;

import com.kimngeam.backend.rag.ingestion.dto.CorpusChunkImportRecord;
import com.kimngeam.backend.shared.entity.SourceType;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

/**
 * Parsea un archivo JSONL de ingesta: una línea, un objeto JSON, shape
 * {@link CorpusChunkJsonlLine}. Cada línea trae {@code contenido} bilingüe
 * completo (prefijos {@code MAP:} / {@code ESP:}), que se persiste tal cual;
 * la extracción del texto a embeder es responsabilidad de
 * {@link CorpusEmbeddingTextExtractor}, no de este parser.
 */
@Component
public class CorpusJsonlParser {

	private final ObjectMapper objectMapper;

	public CorpusJsonlParser(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	public List<CorpusChunkImportRecord> parsear(Path archivo) {
		try (Stream<String> lineas = Files.lines(archivo, StandardCharsets.UTF_8)) {
			return lineas.filter(linea -> !linea.isBlank()).map(linea -> parsearLinea(archivo, linea)).toList();
		} catch (IOException e) {
			throw new CorpusIngestionException("No se pudo leer el archivo JSONL: " + archivo, e);
		}
	}

	private CorpusChunkImportRecord parsearLinea(Path archivo, String linea) {
		CorpusChunkJsonlLine cruda = deserializar(archivo, linea);
		return new CorpusChunkImportRecord(SourceType.fromValue(cruda.sourceType()), cruda.sourceRef(),
				cruda.contenido(), cruda.variante(), cruda.metadata(), cruda.validado());
	}

	private CorpusChunkJsonlLine deserializar(Path archivo, String linea) {
		try {
			return objectMapper.readValue(linea, CorpusChunkJsonlLine.class);
		} catch (JacksonException e) {
			throw new CorpusIngestionException("Línea JSONL inválida en " + archivo + ": " + linea, e);
		}
	}
}
