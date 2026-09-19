package com.kimngeam.backend.rag.embedding;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.ollama.OllamaEmbeddingModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaEmbeddingOptions;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingOptions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;

/**
 * Única fuente del bean {@link EmbeddingModel}: exactamente uno de los dos
 * métodos de abajo se activa según {@code kimngeam.embedding.provider}. Las
 * autoconfiguraciones propias de Spring AI para OpenAI y Ollama están
 * excluidas en {@code application.yml} para que nunca compitan con esta
 * configuración manual ni instancien un bean por su cuenta.
 */
@Configuration
public class EmbeddingModelConfig {

	@Bean
	@ConditionalOnProperty(prefix = "kimngeam.embedding", name = "provider", havingValue = "openrouter")
	public EmbeddingModel openRouterEmbeddingModel(OpenRouterEmbeddingProperties properties) {
		Assert.hasText(properties.apiKey(), "kimngeam.embedding.openrouter.api-key (OPENROUTER_API_KEY) "
				+ "es obligatorio cuando kimngeam.embedding.provider=openrouter");
		Assert.hasText(properties.model(),
				"kimngeam.embedding.openrouter.model es obligatorio cuando kimngeam.embedding.provider=openrouter");
		OpenAiEmbeddingOptions options = OpenAiEmbeddingOptions.builder()
				.baseUrl(properties.baseUrl())
				.apiKey(properties.apiKey())
				.model(properties.model())
				.timeout(properties.timeout())
				.maxRetries(properties.maxRetries())
				.build();
		return OpenAiEmbeddingModel.builder().options(options).build();
	}

	@Bean
	@ConditionalOnProperty(prefix = "kimngeam.embedding", name = "provider", havingValue = "ollama")
	public EmbeddingModel ollamaEmbeddingModel(OllamaEmbeddingProperties properties) {
		Assert.hasText(properties.model(),
				"kimngeam.embedding.ollama.model es obligatorio cuando kimngeam.embedding.provider=ollama");
		OllamaApi ollamaApi = OllamaApi.builder().baseUrl(properties.baseUrl()).build();
		OllamaEmbeddingOptions options = OllamaEmbeddingOptions.builder().model(properties.model()).build();
		return OllamaEmbeddingModel.builder().ollamaApi(ollamaApi).options(options).build();
	}
}
