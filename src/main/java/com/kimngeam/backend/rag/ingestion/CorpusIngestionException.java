package com.kimngeam.backend.rag.ingestion;

/**
 * Error irrecuperable del job de ingesta (archivo ilegible, línea JSONL
 * inválida, ruta del corpus mal configurada). No es una excepción de dominio
 * de la API — este job no corre en el hilo de un request HTTP.
 */
public class CorpusIngestionException extends RuntimeException {

	public CorpusIngestionException(String message) {
		super(message);
	}

	public CorpusIngestionException(String message, Throwable cause) {
		super(message, cause);
	}
}
