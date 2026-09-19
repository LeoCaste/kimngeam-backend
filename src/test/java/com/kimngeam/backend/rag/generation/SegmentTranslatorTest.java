package com.kimngeam.backend.rag.generation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SegmentTranslatorTest {

	@Mock
	private TranslationLlmClient llmClient;

	@Mock
	private TranslationResponseParser responseParser;

	@Test
	void devuelveElResultadoAlPrimerIntentoSiParsea() {
		SegmentTranslator segmentTranslator = new SegmentTranslator(llmClient, responseParser,
				new TranslationParsingProperties(2));
		TranslationLlmResponse respuesta = new TranslationLlmResponse("{...}", "openrouter:google/gemini-3.8-flash");
		SegmentTranslationResult resultado = new SegmentTranslationResult("Mari mari", null);
		when(llmClient.generar(anyString())).thenReturn(respuesta);
		when(responseParser.parsear(respuesta.texto())).thenReturn(resultado);

		SegmentTranslationOutcome outcome = segmentTranslator.generar("prompt");

		assertThat(outcome.resultado()).isEqualTo(resultado);
		assertThat(outcome.modeloLlm()).isEqualTo("openrouter:google/gemini-3.8-flash");
		verify(llmClient, times(1)).generar(anyString());
	}

	@Test
	void reintentaSiElParseoFallaYLuegoTieneExito() {
		SegmentTranslator segmentTranslator = new SegmentTranslator(llmClient, responseParser,
				new TranslationParsingProperties(2));
		TranslationLlmResponse respuesta = new TranslationLlmResponse("{...}", "openrouter:google/gemini-3.8-flash");
		SegmentTranslationResult resultado = new SegmentTranslationResult("Mari mari", null);
		when(llmClient.generar(anyString())).thenReturn(respuesta);
		when(responseParser.parsear(respuesta.texto()))
				.thenThrow(new TranslationParsingException("JSON malformado"))
				.thenReturn(resultado);

		SegmentTranslationOutcome outcome = segmentTranslator.generar("prompt");

		assertThat(outcome.resultado()).isEqualTo(resultado);
		verify(llmClient, times(2)).generar(anyString());
	}

	@Test
	void fallaExplicitoTrasAgotarLosReintentos() {
		SegmentTranslator segmentTranslator = new SegmentTranslator(llmClient, responseParser,
				new TranslationParsingProperties(2));
		TranslationLlmResponse respuesta = new TranslationLlmResponse("{...}", "openrouter:google/gemini-3.8-flash");
		when(llmClient.generar(anyString())).thenReturn(respuesta);
		when(responseParser.parsear(respuesta.texto()))
				.thenThrow(new TranslationParsingException("JSON malformado"));

		assertThatThrownBy(() -> segmentTranslator.generar("prompt")).isInstanceOf(TranslationParsingException.class);
		verify(llmClient, times(3)).generar(anyString());
	}
}
