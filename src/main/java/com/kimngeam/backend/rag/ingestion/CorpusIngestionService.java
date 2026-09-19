package com.kimngeam.backend.rag.ingestion;

import com.kimngeam.backend.rag.embedding.EmbeddingModelIdentifier;
import com.kimngeam.backend.rag.ingestion.dto.CorpusChunkImportRecord;
import com.kimngeam.backend.shared.entity.CorpusChunk;
import com.kimngeam.backend.shared.entity.CorpusChunkRepository;
import com.kimngeam.backend.shared.entity.SourceType;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * Carga el corpus desde los JSONL de {@code kimngeam.corpus.path} a
 * {@code corpus_chunk}. Re-ejecutable sin duplicar: por cada
 * {@link SourceType} presente en los archivos leídos, primero borra los
 * chunks existentes de ese tipo y después inserta los nuevos — necesario
 * porque cambiar de modelo de embeddings implica regenerar todo el corpus.
 * No toca chunks {@code expert_feedback} salvo que también vengan en el
 * JSONL, así que las validaciones de Fase 4 no se pisan con una recarga.
 */
@Service
@ConditionalOnProperty(prefix = "kimngeam.corpus.ingestion", name = "enabled", havingValue = "true")
public class CorpusIngestionService {

	private static final String SUFIJO_JSONL = ".jsonl";

	private final CorpusChunkRepository corpusChunkRepository;
	private final CorpusJsonlParser corpusJsonlParser;
	private final CorpusEmbeddingTextExtractor embeddingTextExtractor;
	private final CorpusChunkEmbeddingWriter embeddingWriter;
	private final EmbeddingModel embeddingModel;
	private final EmbeddingModelIdentifier embeddingModelIdentifier;
	private final CorpusIngestionProperties ingestionProperties;

	public CorpusIngestionService(CorpusChunkRepository corpusChunkRepository, CorpusJsonlParser corpusJsonlParser,
			CorpusEmbeddingTextExtractor embeddingTextExtractor, CorpusChunkEmbeddingWriter embeddingWriter,
			EmbeddingModel embeddingModel, EmbeddingModelIdentifier embeddingModelIdentifier,
			CorpusIngestionProperties ingestionProperties) {
		this.corpusChunkRepository = corpusChunkRepository;
		this.corpusJsonlParser = corpusJsonlParser;
		this.embeddingTextExtractor = embeddingTextExtractor;
		this.embeddingWriter = embeddingWriter;
		this.embeddingModel = embeddingModel;
		this.embeddingModelIdentifier = embeddingModelIdentifier;
		this.ingestionProperties = ingestionProperties;
	}

	public void ingestarDesde(Path directorioCorpus) {
		Map<SourceType, List<CorpusChunkImportRecord>> registrosPorTipo = leerArchivosJsonl(directorioCorpus).stream()
				.collect(Collectors.groupingBy(CorpusChunkImportRecord::sourceType));
		registrosPorTipo.forEach(this::recargarTipo);
	}

	private List<CorpusChunkImportRecord> leerArchivosJsonl(Path directorioCorpus) {
		try (Stream<Path> archivos = Files.list(directorioCorpus)) {
			return archivos.filter(archivo -> archivo.toString().endsWith(SUFIJO_JSONL))
					.sorted()
					.flatMap(archivo -> corpusJsonlParser.parsear(archivo).stream())
					.toList();
		} catch (IOException e) {
			throw new CorpusIngestionException("No se pudo listar el directorio del corpus: " + directorioCorpus, e);
		}
	}

	private void recargarTipo(SourceType sourceType, List<CorpusChunkImportRecord> registros) {
		corpusChunkRepository.deleteBySourceType(sourceType);
		int tamanoLote = ingestionProperties.batchSize();
		for (int inicio = 0; inicio < registros.size(); inicio += tamanoLote) {
			procesarLote(registros.subList(inicio, Math.min(inicio + tamanoLote, registros.size())));
		}
	}

	private void procesarLote(List<CorpusChunkImportRecord> lote) {
		List<CorpusChunk> guardados = corpusChunkRepository.saveAll(lote.stream().map(this::aEntidad).toList());
		List<String> textosEmbedding = lote.stream()
				.map(registro -> embeddingTextExtractor.extraerTextoParaEmbedding(registro.contenido()))
				.toList();
		List<float[]> vectores = generarEmbeddings(textosEmbedding);
		escribirEmbeddings(guardados, vectores);
	}

	private List<float[]> generarEmbeddings(List<String> textos) {
		try {
			return embeddingModel.embed(textos);
		} catch (RuntimeException e) {
			throw new CorpusIngestionException(
					"Fallo generando embeddings para un lote de " + textos.size() + " chunks", e);
		}
	}

	private void escribirEmbeddings(List<CorpusChunk> guardados, List<float[]> vectores) {
		String modeloEmbedding = embeddingModelIdentifier.identificador();
		for (int i = 0; i < guardados.size(); i++) {
			embeddingWriter.escribir(guardados.get(i).getId(), vectores.get(i), modeloEmbedding);
		}
	}

	private CorpusChunk aEntidad(CorpusChunkImportRecord registro) {
		return new CorpusChunk(registro.sourceType(), registro.sourceRef(), registro.contenido(), registro.variante(),
				registro.metadata(), registro.validado());
	}
}
