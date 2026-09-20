package com.kimngeam.backend.traductor;

import static org.assertj.core.api.Assertions.assertThat;

import com.kimngeam.backend.rag.generation.TranslationLlmProperties;
import com.kimngeam.backend.shared.entity.ContextoCultural;
import com.kimngeam.backend.shared.entity.ContextoCulturalRepository;
import com.kimngeam.backend.shared.entity.Direccion;
import com.kimngeam.backend.shared.entity.Traduccion;
import com.kimngeam.backend.shared.entity.TraduccionFuente;
import com.kimngeam.backend.shared.entity.TraduccionFuenteRepository;
import com.kimngeam.backend.shared.entity.TraduccionRepository;
import com.kimngeam.backend.shared.entity.TraduccionSegmento;
import com.kimngeam.backend.shared.entity.TraduccionSegmentoRepository;
import java.util.List;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

/**
 * Punta a punta contra el pipeline real: Postgres + Ollama (embeddings) +
 * el proveedor de LLM activo (OpenRouter por defecto — llamada real, paga).
 * Tag {@code llm-real}: excluido del build normal (ver pom.xml,
 * maven-surefire-plugin), se corre a mano con
 * {@code ./mvnw test -Dgroups=llm-real -Dtest=TraductorServiceIntegrationTest}.
 * {@code @Transactional} para que Spring haga rollback al final de cada test
 * — nada de esto debe quedar persistido en la base real. TraduccionPersistor
 * ya usa @Transactional propagation REQUIRED, así que se une a la transacción
 * del test en vez de abrir la suya.
 */
@SpringBootTest
@Transactional
@Tag("llm-real")
class TraductorServiceIntegrationTest {

	@Autowired
	private TraductorService traductorService;

	@Autowired
	private TraduccionRepository traduccionRepository;

	@Autowired
	private TraduccionSegmentoRepository traduccionSegmentoRepository;

	@Autowired
	private TraduccionFuenteRepository traduccionFuenteRepository;

	@Autowired
	private ContextoCulturalRepository contextoCulturalRepository;

	@Autowired
	private TranslationLlmProperties translationLlmProperties;

	@Test
	void traduceEsMapYPersisteTrazabilidadCompleta() {
		TraduccionResultado resultado = traductorService.traducir(
				"El pueblo mapuche vive en el sur de Chile desde tiempos ancestrales. Su lengua se llama mapudungun.",
				Direccion.ES_MAP, null);

		Traduccion traduccion = traduccionRepository.findById(resultado.id()).orElseThrow();
		assertThat(traduccion.getDireccion()).isEqualTo(Direccion.ES_MAP);
		assertThat(traduccion.getTextoTraducido()).isNotBlank();
		assertThat(traduccion.getModeloLlm()).startsWith(translationLlmProperties.provider().name().toLowerCase() + ":");

		List<TraduccionSegmento> segmentos = traduccionSegmentoRepository.findByTraduccionIdOrderByOrden(traduccion.getId());
		assertThat(segmentos).isNotEmpty();
		assertThat(segmentos).allSatisfy(segmento -> assertThat(segmento.getTraduccion().getId()).isEqualTo(traduccion.getId()));

		List<TraduccionFuente> fuentes = segmentos.stream()
				.flatMap(segmento -> traduccionFuenteRepository.findBySegmentoId(segmento.getId()).stream())
				.toList();
		boolean algunSegmentoConRespaldo = segmentos.stream().anyMatch(TraduccionSegmento::isConRespaldo);
		if (algunSegmentoConRespaldo) {
			assertThat(fuentes).isNotEmpty();
			assertThat(fuentes).allSatisfy(fuente -> assertThat(fuente.getScore()).isNotNull());
		}

		// direccion = es-map: puede o no traer contexto cultural según lo que
		// el modelo identifique, pero nunca para map-es (ver test siguiente).
		contextoCulturalRepository.findByTraduccion_Id(traduccion.getId())
				.forEach(contexto -> assertThat(contexto.getExpresion()).isNotBlank());
	}

	@Test
	void traduceMapEsSinContextoCultural() {
		TraduccionResultado resultado = traductorService.traducir("Mari mari, chumleymi.", Direccion.MAP_ES, null);

		Traduccion traduccion = traduccionRepository.findById(resultado.id()).orElseThrow();
		assertThat(traduccion.getDireccion()).isEqualTo(Direccion.MAP_ES);
		assertThat(traduccion.getTextoTraducido()).isNotBlank();

		List<ContextoCultural> contextos = contextoCulturalRepository.findByTraduccion_Id(traduccion.getId());
		assertThat(contextos).isEmpty();
		assertThat(resultado.contextos()).isEmpty();
	}
}
