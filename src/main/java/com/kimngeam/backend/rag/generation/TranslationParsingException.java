package com.kimngeam.backend.rag.generation;

/**
 * La respuesta del LLM no se pudo convertir en un {@link SegmentTranslationResult}
 * válido, ni siquiera tras los reintentos configurados
 * ({@code kimngeam.llm.parsing.max-reintentos}). No es una {@code ApiException}
 * a propósito: rag/generation no conoce semántica HTTP, eso lo decide quien
 * llame a {@link SegmentTranslator} (ver CLAUDE.md, "capas explícitas").
 */
public class TranslationParsingException extends RuntimeException {

	public TranslationParsingException(String message) {
		super(message);
	}

	public TranslationParsingException(String message, Throwable cause) {
		super(message, cause);
	}
}
