package com.kimngeam.backend.rag.ingestion;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Configuración del job de carga del corpus. Desactivado por defecto: nunca
 * debe correr solo por levantar la app.
 */
@ConfigurationProperties(prefix = "kimngeam.corpus.ingestion")
public record CorpusIngestionProperties(@DefaultValue("false") boolean enabled, @DefaultValue("200") int batchSize) {
}
