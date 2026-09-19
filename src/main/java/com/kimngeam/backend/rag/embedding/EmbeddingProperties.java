package com.kimngeam.backend.rag.embedding;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Selector del proveedor de embeddings activo. Default {@code ollama} a
 * propósito: un clon nuevo del repo debe poder levantar en desarrollo sin
 * configurar credenciales de OpenRouter (ver CLAUDE.md).
 */
@ConfigurationProperties(prefix = "kimngeam.embedding")
public record EmbeddingProperties(@DefaultValue("ollama") EmbeddingProvider provider) {
}
