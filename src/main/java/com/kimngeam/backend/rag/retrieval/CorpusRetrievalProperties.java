package com.kimngeam.backend.rag.retrieval;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Cuántos chunks trae {@link CorpusRetrievalService} por consulta. Default 5:
 * suficiente margen sobre el top-3 validado empíricamente (ver CLAUDE.md) sin
 * inflar de más el contexto que después arma la generación (Fase 3).
 */
@ConfigurationProperties(prefix = "kimngeam.corpus.retrieval")
public record CorpusRetrievalProperties(@DefaultValue("5") int topN) {
}
