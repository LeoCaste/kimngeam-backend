package com.kimngeam.backend.rag.generation;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Cuántas veces {@link SegmentTranslator} vuelve a pedirle al LLM una
 * traducción si la respuesta anterior no parseó como {@link SegmentTranslationResult}
 * — ej. JSON malformado o envuelto en un bloque de markdown que no se pudo
 * limpiar. Default 2: hasta 3 intentos totales antes de fallar explícito (ver
 * CLAUDE.md, "nunca devolver algo a medias").
 */
@ConfigurationProperties(prefix = "kimngeam.llm.parsing")
public record TranslationParsingProperties(@DefaultValue("2") int maxReintentos) {
}
