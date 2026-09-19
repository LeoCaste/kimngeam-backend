package com.kimngeam.backend.rag.embedding;

/**
 * Proveedores de embeddings soportados, siempre detrás de la abstracción
 * {@code EmbeddingModel} de Spring AI (ver {@link EmbeddingModelConfig}).
 * OpenRouter es la API externa de producción; Ollama corre local y no
 * consume créditos, pensado para desarrollo.
 */
public enum EmbeddingProvider {
	OPENROUTER,
	OLLAMA
}
