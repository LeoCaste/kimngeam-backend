package com.kimngeam.backend.traductor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.kimngeam.backend.rag.generation.ContextoCulturalGenerado;
import com.kimngeam.backend.rag.generation.SegmentTranslationOutcome;
import com.kimngeam.backend.rag.generation.SegmentTranslationResult;
import com.kimngeam.backend.rag.generation.SegmentTranslator;
import com.kimngeam.backend.rag.generation.TranslationPromptBuilder;
import com.kimngeam.backend.rag.retrieval.CorpusRetrievalService;
import com.kimngeam.backend.rag.retrieval.RetrievedCorpusChunk;
import com.kimngeam.backend.shared.entity.Direccion;
import com.kimngeam.backend.shared.entity.SourceType;
import com.kimngeam.backend.shared.error.BadRequestException;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TraductorServiceTest {

	@Mock
	private TextSegmenter textSegmenter;

	@Mock
	private CorpusRetrievalService corpusRetrievalService;

	@Mock
	private TranslationPromptBuilder promptBuilder;

	@Mock
	private SegmentTranslator segmentTranslator;

	@Mock
	private ConfianzaCalculator confianzaCalculator;

	@Mock
	private TraduccionPersistor traduccionPersistor;

	private final SegmentacionProperties segmentacionProperties = new SegmentacionProperties(5000, 200);
	private final TraductorProperties traductorProperties = new TraductorProperties(0.5);

	private TraductorService nuevoServicio() {
		return new TraductorService(textSegmenter, segmentacionProperties, traductorProperties,
				corpusRetrievalService, promptBuilder, segmentTranslator, confianzaCalculator, traduccionPersistor);
	}

	@Test
	void textoVacioEsRechazado() {
		TraductorService service = nuevoServicio();

		assertThatThrownBy(() -> service.traducir("   ", Direccion.ES_MAP, null))
				.isInstanceOf(BadRequestException.class);
	}

	@Test
	void textoQueSuperaElMaximoEsRechazado() {
		TraductorService service = nuevoServicio();
		String textoLargo = "a".repeat(segmentacionProperties.maximoEntrada() + 1);

		assertThatThrownBy(() -> service.traducir(textoLargo, Direccion.ES_MAP, null))
				.isInstanceOf(BadRequestException.class)
				.hasMessageContaining(String.valueOf(textoLargo.length()));
	}

	@Test
	void ordenaSegmentaGeneraYPersisteConLaConfianzaDerivadaDelRetrieval() {
		String segmento1 = "Frase uno.";
		String segmento2 = "Frase dos.";
		when(textSegmenter.segmentar("Frase uno. Frase dos.", segmentacionProperties.umbralSegmentacion()))
				.thenReturn(List.of(segmento1, segmento2));

		RetrievedCorpusChunk chunkConRespaldo = chunk(0.9, true);
		RetrievedCorpusChunk chunkBajoUmbral = chunk(0.1, false);
		when(corpusRetrievalService.buscar(segmento1)).thenReturn(List.of(chunkConRespaldo));
		when(corpusRetrievalService.buscar(segmento2)).thenReturn(List.of(chunkBajoUmbral));

		when(promptBuilder.construir(eq(Direccion.ES_MAP), eq(segmento1), eq(null), eq(null), any()))
				.thenReturn("PROMPT1");
		when(promptBuilder.construir(eq(Direccion.ES_MAP), eq(segmento2), eq(segmento1), eq("Traducido uno"), any()))
				.thenReturn("PROMPT2");

		ContextoCulturalGenerado aporteCultural = new ContextoCulturalGenerado("expresion", "aporte", null, null);
		SegmentTranslationOutcome outcome1 = new SegmentTranslationOutcome(
				new SegmentTranslationResult("Traducido uno", List.of(aporteCultural)),
				"openrouter:google/gemini-3.8-flash");
		SegmentTranslationOutcome outcome2 = new SegmentTranslationOutcome(
				new SegmentTranslationResult("Traducido dos", List.of()), "openrouter:google/gemini-3.8-flash");
		when(segmentTranslator.generar("PROMPT1")).thenReturn(outcome1);
		when(segmentTranslator.generar("PROMPT2")).thenReturn(outcome2);

		BigDecimal confianzaSegmento1 = new BigDecimal("0.90");
		when(confianzaCalculator.calcularSegmento(List.of(chunkConRespaldo))).thenReturn(confianzaSegmento1);
		BigDecimal confianzaGlobal = new BigDecimal("0.45");
		when(confianzaCalculator.calcularGlobal(any())).thenReturn(confianzaGlobal);

		TraduccionResultado resultadoEsperado = new TraduccionResultado("trad_1", "Frase uno. Frase dos.",
				"Traducido uno Traducido dos", Direccion.ES_MAP, confianzaGlobal, List.of(), OffsetDateTime.now(),
				false);
		when(traduccionPersistor.persistir(eq(7L), eq("Frase uno. Frase dos."), eq("Traducido uno Traducido dos"),
				eq(Direccion.ES_MAP), eq(confianzaGlobal), eq("openrouter:google/gemini-3.8-flash"), any()))
				.thenReturn(resultadoEsperado);

		TraductorService service = nuevoServicio();
		TraduccionResultado resultado = service.traducir("Frase uno. Frase dos.", Direccion.ES_MAP, 7L);

		assertThat(resultado).isEqualTo(resultadoEsperado);

		ArgumentCaptor<List<SegmentoProcesado>> captor = ArgumentCaptor.forClass(List.class);
		org.mockito.Mockito.verify(traduccionPersistor)
				.persistir(eq(7L), eq("Frase uno. Frase dos."), eq("Traducido uno Traducido dos"),
						eq(Direccion.ES_MAP), eq(confianzaGlobal), eq("openrouter:google/gemini-3.8-flash"),
						captor.capture());
		List<SegmentoProcesado> segmentosPersistidos = captor.getValue();
		assertThat(segmentosPersistidos).hasSize(2);

		SegmentoProcesado primero = segmentosPersistidos.get(0);
		assertThat(primero.orden()).isEqualTo(1);
		assertThat(primero.conRespaldo()).isTrue();
		assertThat(primero.confianza()).isEqualByComparingTo(confianzaSegmento1);
		assertThat(primero.chunksUsados()).containsExactly(chunkConRespaldo);
		assertThat(primero.contextoCultural()).containsExactly(aporteCultural);

		SegmentoProcesado segundo = segmentosPersistidos.get(1);
		assertThat(segundo.orden()).isEqualTo(2);
		assertThat(segundo.conRespaldo()).isFalse();
		assertThat(segundo.confianza()).isNull();
		assertThat(segundo.chunksUsados()).isEmpty();
	}

	private static RetrievedCorpusChunk chunk(double similitud, boolean validado) {
		return new RetrievedCorpusChunk(UUID.randomUUID(), SourceType.TRANSLATION_EXAMPLES, "ref", "contenido", null,
				similitud, validado);
	}
}
