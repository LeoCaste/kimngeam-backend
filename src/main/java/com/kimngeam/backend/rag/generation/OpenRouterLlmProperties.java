package com.kimngeam.backend.rag.generation;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * OpenRouter expone una API de chat compatible con OpenAI: por eso se arma
 * con el cliente de OpenAI de Spring AI apuntado a esta base-url, en vez de un
 * proveedor nativo (mismo patrón que {@code rag.embedding.OpenRouterEmbeddingProperties}).
 * {@code apiKey} y {@code model} no llevan default en este record — nunca se
 * hardcodea ninguno de los dos; el default no sensible del modelo (ver
 * CLAUDE.md, docs/TODO.md Fase 3) vive en {@code application.yml}. El timeout
 * por defecto es alto porque las llamadas reales midieron entre 3 y 90
 * segundos según el modelo.
 */
@ConfigurationProperties(prefix = "kimngeam.llm.openrouter")
public record OpenRouterLlmProperties(
		@DefaultValue("https://openrouter.ai/api/v1") String baseUrl,
		String apiKey,
		String model,
		@DefaultValue("90s") Duration timeout,
		@DefaultValue("2") int maxRetries) {
}
