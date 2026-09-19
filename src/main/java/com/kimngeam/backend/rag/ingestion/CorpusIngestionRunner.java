package com.kimngeam.backend.rag.ingestion;

import java.nio.file.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Job de ingesta manual, desactivado por defecto
 * ({@code kimngeam.corpus.ingestion.enabled=false}). Activarlo hoy fallaría
 * al arrancar salvo que además haya un {@code EmbeddingModel} de Spring AI
 * configurado (ningún proveedor está elegido todavía, ver CLAUDE.md) — es el
 * comportamiento esperado, no un bug: el modelo se resuelve en Fase 2.
 */
@Component
@ConditionalOnProperty(prefix = "kimngeam.corpus.ingestion", name = "enabled", havingValue = "true")
public class CorpusIngestionRunner implements CommandLineRunner {

	private static final Logger log = LoggerFactory.getLogger(CorpusIngestionRunner.class);

	private final CorpusIngestionService corpusIngestionService;
	private final CorpusProperties corpusProperties;

	public CorpusIngestionRunner(CorpusIngestionService corpusIngestionService, CorpusProperties corpusProperties) {
		this.corpusIngestionService = corpusIngestionService;
		this.corpusProperties = corpusProperties;
	}

	@Override
	public void run(String... args) {
		String rutaCorpus = corpusProperties.path();
		if (rutaCorpus == null || rutaCorpus.isBlank()) {
			throw new CorpusIngestionException(
					"kimngeam.corpus.ingestion.enabled=true pero KIMNGEAM_CORPUS_PATH no está configurada");
		}
		log.info("Iniciando ingesta del corpus desde {}", rutaCorpus);
		corpusIngestionService.ingestarDesde(Path.of(rutaCorpus));
		log.info("Ingesta del corpus finalizada");
	}
}
