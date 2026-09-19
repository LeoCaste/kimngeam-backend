package com.kimngeam.backend.rag.generation;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * SOLO para testeo local (ver CLAUDE.md, docs/TODO.md Fase 3) — nunca el
 * proveedor de generación en producción. {@code model} no lleva default:
 * ninguna parte del código hardcodea el modelo. Requiere tener Ollama
 * corriendo con el modelo ya descargado (ej. {@code ollama pull llama3.2}).
 */
@ConfigurationProperties(prefix = "kimngeam.llm.ollama")
public record OllamaLlmProperties(
		@DefaultValue("http://localhost:11434") String baseUrl,
		String model,
		@DefaultValue("90s") Duration timeout,
		@DefaultValue("1") int maxRetries) {
}
