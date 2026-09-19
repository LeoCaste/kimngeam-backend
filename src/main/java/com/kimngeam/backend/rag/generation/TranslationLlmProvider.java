package com.kimngeam.backend.rag.generation;

/**
 * Proveedores de generación soportados. OpenRouter es la API externa de
 * producción (candidatos evaluados: google/gemini-3.8-flash,
 * google/gemini-3.1-flash-lite, openai/gpt-5.4-mini); Ollama corre local y es
 * SOLO para testeo, nunca para producción (ver CLAUDE.md, docs/TODO.md Fase 3).
 */
public enum TranslationLlmProvider {
	OPENROUTER,
	OLLAMA
}
