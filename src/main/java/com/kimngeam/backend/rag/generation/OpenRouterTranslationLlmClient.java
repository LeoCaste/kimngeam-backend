package com.kimngeam.backend.rag.generation;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * Implementación de producción de {@link TranslationLlmClient}: OpenRouter
 * expone una API de chat compatible con OpenAI, por eso se arma con
 * {@link OpenAiChatModel} apuntado a su base-url (mismo patrón que el
 * {@code EmbeddingModel} de OpenRouter en {@code rag.embedding}). El timeout y
 * los reintentos quedan resueltos por el cliente oficial de OpenAI que Spring
 * AI construye internamente a partir de {@link OpenAiChatOptions}.
 */
@Component
@ConditionalOnProperty(prefix = "kimngeam.llm", name = "provider", havingValue = "openrouter")
public class OpenRouterTranslationLlmClient implements TranslationLlmClient {

	private final ChatModel chatModel;
	private final String modeloIdentificador;

	public OpenRouterTranslationLlmClient(OpenRouterLlmProperties properties) {
		Assert.hasText(properties.apiKey(),
				"kimngeam.llm.openrouter.api-key (OPENROUTER_API_KEY) es obligatorio cuando kimngeam.llm.provider=openrouter");
		Assert.hasText(properties.model(),
				"kimngeam.llm.openrouter.model es obligatorio cuando kimngeam.llm.provider=openrouter");
		OpenAiChatOptions options = OpenAiChatOptions.builder()
				.baseUrl(properties.baseUrl())
				.apiKey(properties.apiKey())
				.model(properties.model())
				.timeout(properties.timeout())
				.maxRetries(properties.maxRetries())
				.build();
		this.chatModel = OpenAiChatModel.builder().options(options).build();
		this.modeloIdentificador = "openrouter:" + properties.model();
	}

	@Override
	public TranslationLlmResponse generar(String prompt) {
		String texto = chatModel.call(prompt);
		return new TranslationLlmResponse(texto, modeloIdentificador);
	}
}
