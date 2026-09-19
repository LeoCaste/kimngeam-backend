package com.kimngeam.backend.rag.generation;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

/**
 * Convierte el texto crudo que devuelve el LLM en un {@link SegmentTranslationResult}.
 * En pruebas reales varios modelos envuelven el JSON en un bloque de markdown
 * ({@code ```json ... ```}) pese a que el prompt pide JSON puro, así que ese
 * envoltorio se limpia antes de intentar parsear (ver CLAUDE.md, Fase 3).
 */
@Component
public class TranslationResponseParser {

	private static final Pattern BLOQUE_MARKDOWN = Pattern.compile("^```(?:json)?\\s*\\n?(.*?)\\n?```$",
			Pattern.DOTALL);

	private final ObjectMapper objectMapper;

	public TranslationResponseParser(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	public SegmentTranslationResult parsear(String respuestaCruda) {
		String json = limpiarBloqueMarkdown(respuestaCruda);
		SegmentTranslationResult resultado = deserializar(json);
		validar(resultado);
		return resultado;
	}

	private String limpiarBloqueMarkdown(String respuestaCruda) {
		String texto = respuestaCruda.strip();
		Matcher matcher = BLOQUE_MARKDOWN.matcher(texto);
		return matcher.matches() ? matcher.group(1).strip() : texto;
	}

	private SegmentTranslationResult deserializar(String json) {
		try {
			return objectMapper.readValue(json, SegmentTranslationResult.class);
		}
		catch (JacksonException e) {
			throw new TranslationParsingException("La respuesta del modelo no es JSON válido: " + e.getMessage(), e);
		}
	}

	private void validar(SegmentTranslationResult resultado) {
		if (resultado.traduccion() == null || resultado.traduccion().isBlank()) {
			throw new TranslationParsingException("La respuesta del modelo no incluye una traducción válida");
		}
	}
}
