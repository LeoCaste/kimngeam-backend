package com.kimngeam.backend.rag.generation;

/**
 * Puerto hacia el modelo generador de la traducción. Cada proveedor
 * (OpenRouter, Ollama, y a futuro Google AI Studio u Ofox AI) es una
 * implementación propia de esta interfaz, seleccionada por
 * {@code kimngeam.llm.provider} — sumar un proveedor nuevo es solo una clase
 * más, sin tocar el resto del código (ver CLAUDE.md y docs/TODO.md, Fase 3).
 */
public interface TranslationLlmClient {

	TranslationLlmResponse generar(String prompt);
}
