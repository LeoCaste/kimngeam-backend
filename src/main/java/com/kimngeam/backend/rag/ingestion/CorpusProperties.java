package com.kimngeam.backend.rag.ingestion;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Ruta a la carpeta con las fuentes del corpus (diccionario, transcripciones,
 * JSONL preprocesado). Vive fuera del repo (~240 MB) — ver CLAUDE.md,
 * "Fuentes de datos del corpus". Nunca una ruta relativa hardcodeada: se
 * configura por {@code KIMNGEAM_CORPUS_PATH}.
 * <p>
 * Sin default y sin validación acá a propósito: mientras la ingesta esté
 * desactivada ({@code kimngeam.corpus.ingestion.enabled=false}) la app debe
 * arrancar igual aunque la variable no esté seteada. Quien sí exige que venga
 * seteada es {@code CorpusIngestionRunner}, al momento de ejecutarse.
 */
@ConfigurationProperties(prefix = "kimngeam.corpus")
public record CorpusProperties(String path) {
}
