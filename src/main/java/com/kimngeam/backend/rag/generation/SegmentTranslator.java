package com.kimngeam.backend.rag.generation;

import org.springframework.stereotype.Service;

/**
 * Genera y parsea la traducción de un segmento, reintentando ante una
 * respuesta que no parseó (JSON malformado, shape inesperado) — nunca
 * devuelve un resultado a medias (ver CLAUDE.md, Fase 3). Cada intento vuelve
 * a llamar al LLM con el mismo prompt: no hay garantía de que repita el
 * mismo error de formato.
 */
@Service
public class SegmentTranslator {

	private final TranslationLlmClient llmClient;
	private final TranslationResponseParser responseParser;
	private final TranslationParsingProperties parsingProperties;

	public SegmentTranslator(TranslationLlmClient llmClient, TranslationResponseParser responseParser,
			TranslationParsingProperties parsingProperties) {
		this.llmClient = llmClient;
		this.responseParser = responseParser;
		this.parsingProperties = parsingProperties;
	}

	public SegmentTranslationOutcome generar(String prompt) {
		int maxIntentos = parsingProperties.maxReintentos() + 1;
		TranslationParsingException ultimoError = null;
		for (int intento = 1; intento <= maxIntentos; intento++) {
			TranslationLlmResponse respuesta = llmClient.generar(prompt);
			try {
				SegmentTranslationResult resultado = responseParser.parsear(respuesta.texto());
				return new SegmentTranslationOutcome(resultado, respuesta.modelo());
			}
			catch (TranslationParsingException e) {
				ultimoError = e;
			}
		}
		throw new TranslationParsingException(
				"El modelo no devolvió una respuesta parseable tras " + maxIntentos + " intentos", ultimoError);
	}
}
