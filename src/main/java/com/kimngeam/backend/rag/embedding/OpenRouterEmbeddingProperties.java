package com.kimngeam.backend.rag.embedding;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * OpenRouter expone una API de embeddings compatible con OpenAI: por eso se
 * arma con el cliente de OpenAI de Spring AI apuntado a esta base-url, en vez
 * de un proveedor nativo. {@code apiKey} y {@code model} no llevan default:
 * ninguno se hardcodea (ver CLAUDE.md) y se validan recién al construir el
 * bean, solo si este es el proveedor activo (ver {@link EmbeddingModelConfig}).
 */
@ConfigurationProperties(prefix = "kimngeam.embedding.openrouter")
public record OpenRouterEmbeddingProperties(
		@DefaultValue("https://openrouter.ai/api/v1") String baseUrl,
		String apiKey,
		String model,
		@DefaultValue("30s") Duration timeout,
		@DefaultValue("3") int maxRetries) {
}
