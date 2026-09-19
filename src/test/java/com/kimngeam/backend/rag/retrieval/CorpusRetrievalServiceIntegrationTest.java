package com.kimngeam.backend.rag.retrieval;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Diez consultas de resultado conocido sobre el corpus real de transcripciones
 * orales (fuente: {@code translation_examples.jsonl}, cargado con
 * {@code CorpusIngestionRunner}), validadas manualmente en un experimento
 * previo con el mismo modelo de embeddings: el chunk mapudungun esperado
 * apareció en el top-3 en los diez casos. Requiere Postgres con ese corpus ya
 * ingerido y el proveedor de embeddings activo ({@code kimngeam.embedding.provider})
 * siendo el mismo que lo indexó — comparar contra un proveedor distinto no es
 * un test de retrieval válido (ver CLAUDE.md).
 */
@SpringBootTest
class CorpusRetrievalServiceIntegrationTest {

	private static final int TOP_N_ESPERADO = 3;

	@Autowired
	private CorpusRetrievalService corpusRetrievalService;

	static Stream<Arguments> consultasDeResultadoConocido() {
		return Stream.of(
				Arguments.of("recolectó hierbas medicinales de una vertiente con dueño", "menoko l'awen'"),
				Arguments.of("se me murieron cuatro guaguas pequeñas", "meli püñeñ ta perdelpan"),
				Arguments.of("soñé con una piedra enorme y unos gatos", "fütake küra"),
				Arguments.of("trajeron un tambor ceremonial para hacer la sanación", "küpali ni kultrung"),
				Arguments.of("qué planta sirve para bajar la fiebre en infusión", "wella pingekelu"),
				Arguments.of("diferencia entre agua hervida y agua sin hervir", "re karüko"),
				Arguments.of("después de la ceremonia dejé de tener pesadillas", "wecha pewmawetulan"),
				Arguments.of("me enfermé siendo una niña de ocho años", "pura tripantu"),
				Arguments.of("mi padre curaba con oraciones", "muyatuñmarkenew"),
				Arguments.of("un perro que parecía gato me olfateaba", "ñayki trewa"));
	}

	@ParameterizedTest(name = "\"{0}\" recupera el chunk de \"{1}\" en el top-3")
	@MethodSource("consultasDeResultadoConocido")
	void elChunkEsperadoApareceEnElTop3(String consulta, String fragmentoMapudungunEsperado) {
		List<RetrievedCorpusChunk> resultados = corpusRetrievalService.buscar(consulta);

		assertThat(resultados).hasSizeGreaterThanOrEqualTo(TOP_N_ESPERADO);

		List<RetrievedCorpusChunk> top3 = resultados.subList(0, TOP_N_ESPERADO);
		assertThat(top3).as("top-3 para la consulta \"%s\"", consulta)
				.anyMatch(chunk -> chunk.contenido().contains(fragmentoMapudungunEsperado));
	}
}
