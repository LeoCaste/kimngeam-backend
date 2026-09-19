package com.kimngeam.backend.rag.generation;

import java.net.http.HttpClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.client.RestClient;

/**
 * Implementación SOLO para testeo local de {@link TranslationLlmClient} (ver
 * CLAUDE.md, docs/TODO.md Fase 3) — nunca para producción. A diferencia de
 * OpenRouter, el cliente HTTP de Ollama no trae reintentos incorporados, así
 * que {@link #generar(String)} los implementa a mano de forma simple.
 */
@Component
@ConditionalOnProperty(prefix = "kimngeam.llm", name = "provider", havingValue = "ollama")
public class OllamaTranslationLlmClient implements TranslationLlmClient {

	private final ChatModel chatModel;
	private final String modeloIdentificador;
	private final int maxRetries;

	public OllamaTranslationLlmClient(OllamaLlmProperties properties) {
		Assert.hasText(properties.model(),
				"kimngeam.llm.ollama.model es obligatorio cuando kimngeam.llm.provider=ollama");
		JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(
				HttpClient.newBuilder().connectTimeout(properties.timeout()).build());
		requestFactory.setReadTimeout(properties.timeout());
		OllamaApi ollamaApi = OllamaApi.builder()
				.baseUrl(properties.baseUrl())
				.restClientBuilder(RestClient.builder().requestFactory(requestFactory))
				.build();
		OllamaChatOptions options = OllamaChatOptions.builder().model(properties.model()).build();
		this.chatModel = OllamaChatModel.builder().ollamaApi(ollamaApi).options(options).build();
		this.modeloIdentificador = "ollama:" + properties.model();
		this.maxRetries = properties.maxRetries();
	}

	@Override
	public TranslationLlmResponse generar(String prompt) {
		RuntimeException ultimoError = null;
		for (int intento = 0; intento <= maxRetries; intento++) {
			try {
				String texto = chatModel.call(prompt);
				return new TranslationLlmResponse(texto, modeloIdentificador);
			}
			catch (RuntimeException e) {
				ultimoError = e;
			}
		}
		throw ultimoError;
	}
}
