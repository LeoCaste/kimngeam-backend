package com.kimngeam.backend.rag.generation;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Selector del proveedor de generación activo. A diferencia de
 * {@code kimngeam.embedding.provider}, acá el default es {@code openrouter}:
 * un modelo de chat local chico produce mapudungun inutilizable (no pasa lo
 * mismo con embeddings, donde el local sí sirve), así que Ollama no aporta
 * como fallback de desarrollo y solo obligaría a descargar un modelo de chat
 * para que la app arranque. Ollama sigue disponible, seleccionable por
 * configuración, pero SOLO para testeo local — nunca en producción.
 */
@ConfigurationProperties(prefix = "kimngeam.llm")
public record TranslationLlmProperties(@DefaultValue("openrouter") TranslationLlmProvider provider) {
}
