package com.kimngeam.backend.rag.embedding;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * {@code model} no lleva default a propósito: ninguna parte del código
 * hardcodea el modelo de embeddings (ver CLAUDE.md). Se valida recién al
 * construir el bean, solo si Ollama es el proveedor activo (ver
 * {@link EmbeddingModelConfig}).
 */
@ConfigurationProperties(prefix = "kimngeam.embedding.ollama")
public record OllamaEmbeddingProperties(@DefaultValue("http://localhost:11434") String baseUrl, String model) {
}
