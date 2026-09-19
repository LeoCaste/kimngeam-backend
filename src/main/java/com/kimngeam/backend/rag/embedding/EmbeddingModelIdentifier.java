package com.kimngeam.backend.rag.embedding;

import org.springframework.stereotype.Component;

/**
 * Identificador "proveedor:modelo" (ej. {@code openrouter:baai/bge-m3}) que
 * se guarda en {@code corpus_chunk.modelo_embedding}. Aunque dos proveedores
 * sirvan nominalmente el mismo modelo, sus vectores no son necesariamente
 * idénticos — de ahí que el proveedor sea parte del identificador, no solo
 * el nombre del modelo.
 */
@Component
public class EmbeddingModelIdentifier {

	private final EmbeddingProperties embeddingProperties;
	private final OpenRouterEmbeddingProperties openRouterProperties;
	private final OllamaEmbeddingProperties ollamaProperties;

	public EmbeddingModelIdentifier(EmbeddingProperties embeddingProperties,
			OpenRouterEmbeddingProperties openRouterProperties, OllamaEmbeddingProperties ollamaProperties) {
		this.embeddingProperties = embeddingProperties;
		this.openRouterProperties = openRouterProperties;
		this.ollamaProperties = ollamaProperties;
	}

	public String identificador() {
		String modelo = switch (embeddingProperties.provider()) {
			case OPENROUTER -> openRouterProperties.model();
			case OLLAMA -> ollamaProperties.model();
		};
		return embeddingProperties.provider().name().toLowerCase() + ":" + modelo;
	}
}
