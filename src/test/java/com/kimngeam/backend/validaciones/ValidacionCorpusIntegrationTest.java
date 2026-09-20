package com.kimngeam.backend.validaciones;

import static org.assertj.core.api.Assertions.assertThat;

import com.kimngeam.backend.rag.embedding.EmbeddingModelIdentifier;
import com.kimngeam.backend.rag.retrieval.CorpusRetrievalService;
import com.kimngeam.backend.rag.retrieval.RetrievedCorpusChunk;
import com.kimngeam.backend.shared.entity.CorpusChunk;
import com.kimngeam.backend.shared.entity.CorpusChunkRepository;
import com.kimngeam.backend.shared.entity.Direccion;
import com.kimngeam.backend.shared.entity.SourceType;
import com.kimngeam.backend.shared.entity.Traduccion;
import com.kimngeam.backend.shared.entity.TraduccionRepository;
import com.kimngeam.backend.shared.entity.Usuario;
import com.kimngeam.backend.shared.entity.UsuarioRepository;
import com.kimngeam.backend.traductor.ConfianzaCalculator;
import com.kimngeam.backend.validaciones.dto.ValidacionResponse;
import com.kimngeam.backend.validaciones.dto.ValidarExpresionRequest;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

/**
 * El loop completo de curación (ver CLAUDE.md): una validación real entra al
 * corpus y una consulta de retrieval sobre el mismo tema la recupera. Esta es
 * la prueba que demuestra que el sistema cumple su objetivo — si falla, el
 * proyecto no hace lo que dice hacer. Solo usa Postgres + Ollama (embeddings,
 * local y gratis) — nada de LLM real, así que corre en el build normal.
 * {@code @Transactional} para que Spring haga rollback al final de cada test.
 */
@SpringBootTest
@Transactional
class ValidacionCorpusIntegrationTest {

	private static final String EMAIL_ACADEMICO = "tefi@ufro.cl";

	@Autowired
	private ValidacionService validacionService;

	@Autowired
	private ValidacionCorpusIndexer validacionCorpusIndexer;

	@Autowired
	private TraduccionRepository traduccionRepository;

	@Autowired
	private UsuarioRepository usuarioRepository;

	@Autowired
	private CorpusChunkRepository corpusChunkRepository;

	@Autowired
	private CorpusRetrievalService corpusRetrievalService;

	@Autowired
	private ConfianzaCalculator confianzaCalculator;

	@Autowired
	private EmbeddingModelIdentifier embeddingModelIdentifier;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	void unaValidacionEntraAlCorpusYSeRecuperaEnUnaConsultaSobreElMismoTema() {
		Usuario academico = usuarioRepository.findByEmail(EMAIL_ACADEMICO).orElseThrow();
		Traduccion traduccion = traduccionRepository
				.save(new Traduccion(academico, "origen", "traducido", Direccion.ES_MAP, null, null));

		String expresion = "trarilonko";
		String comentario = "El trarilonko es la cinta o vincha ceremonial que usan las mujeres mapuche en la "
				+ "cabeza, asociada a ceremonias y al rol de la machi en algunas comunidades.";
		ValidarExpresionRequest request = new ValidarExpresionRequest(traduccion.getId(), expresion, comentario,
				"Nguluche");

		ValidacionResponse response = validacionService.registrarExpresion(request, academico.getId());

		CorpusChunk chunk = corpusChunkRepository
				.findBySourceTypeAndSourceRef(SourceType.EXPERT_FEEDBACK, response.id())
				.orElseThrow();
		assertThat(chunk.getSourceType()).isEqualTo(SourceType.EXPERT_FEEDBACK);
		assertThat(chunk.isValidado()).isTrue();
		assertThat(chunk.isActivo()).isTrue();
		assertThat(chunk.getSourceRef()).isEqualTo(response.id());
		assertThat(chunk.getValidadoPor().getId()).isEqualTo(academico.getId());
		assertThat(chunk.getValidadoEn()).isNotNull();
		assertThat(chunk.getContenido()).contains(expresion).contains(comentario);

		// modelo_embedding y el vector se escriben por JDBC directo (ver
		// ValidacionCorpusIndexer / CorpusChunkEmbeddingWriter), así que no se
		// leen de vuelta de forma confiable desde la misma entidad JPA ya en
		// el contexto de persistencia — se verifican con una consulta aparte.
		String modeloEmbedding = jdbcTemplate.queryForObject(
				"SELECT modelo_embedding FROM corpus_chunk WHERE id = ?", String.class, chunk.getId());
		assertThat(modeloEmbedding).isEqualTo(embeddingModelIdentifier.identificador());
		Boolean tieneEmbedding = jdbcTemplate.queryForObject(
				"SELECT embedding IS NOT NULL FROM corpus_chunk WHERE id = ?", Boolean.class, chunk.getId());
		assertThat(tieneEmbedding).isTrue();

		List<RetrievedCorpusChunk> recuperados = corpusRetrievalService
				.buscar("¿Qué es el trarilonko y para qué se usa?");
		assertThat(recuperados).anySatisfy(recuperado -> assertThat(recuperado.id()).isEqualTo(chunk.getId()));

		RetrievedCorpusChunk chunkValidado = recuperados.stream()
				.filter(recuperado -> recuperado.id().equals(chunk.getId()))
				.findFirst()
				.orElseThrow();
		assertThat(chunkValidado.validado()).isTrue();

		// La confianza debe ponderar más un chunk validado que uno sin
		// revisar (ver ConfianzaCalculator, Fase 3 bloque 2) — confirmado acá
		// con datos reales: el mismo chunk recuperado, comparado contra sí
		// mismo con validado = false, produce una confianza menor siempre que
		// haya al menos otro chunk con menor score en la lista.
		if (recuperados.size() > 1) {
			BigDecimal confianzaConValidado = confianzaCalculator.calcularSegmento(recuperados);
			List<RetrievedCorpusChunk> sinValidar = recuperados.stream()
					.map(recuperado -> recuperado.id().equals(chunk.getId())
							? new RetrievedCorpusChunk(recuperado.id(), recuperado.sourceType(),
									recuperado.sourceRef(), recuperado.contenido(), recuperado.variante(),
									recuperado.similitud(), false)
							: recuperado)
					.toList();
			BigDecimal confianzaSinValidar = confianzaCalculator.calcularSegmento(sinValidar);
			assertThat(confianzaConValidado).isGreaterThan(confianzaSinValidar);
		}
	}

	@Test
	void revertirDesactivaElChunkYLoSacaDelRetrieval() {
		Usuario academico = usuarioRepository.findByEmail(EMAIL_ACADEMICO).orElseThrow();
		Traduccion traduccion = traduccionRepository
				.save(new Traduccion(academico, "origen", "traducido", Direccion.ES_MAP, null, null));
		String expresion = "kintuwe";
		String comentario = "El kintuwe es un objeto ritual usado en prácticas de búsqueda ceremonial mapuche.";
		ValidarExpresionRequest request = new ValidarExpresionRequest(traduccion.getId(), expresion, comentario,
				null);
		ValidacionResponse response = validacionService.registrarExpresion(request, academico.getId());

		validacionCorpusIndexer.revertir(response.id());

		CorpusChunk chunk = corpusChunkRepository
				.findBySourceTypeAndSourceRef(SourceType.EXPERT_FEEDBACK, response.id())
				.orElseThrow();
		assertThat(chunk.isActivo()).isFalse();

		List<RetrievedCorpusChunk> recuperados = corpusRetrievalService.buscar("¿Qué es el kintuwe?");
		assertThat(recuperados).noneSatisfy(recuperado -> assertThat(recuperado.id()).isEqualTo(chunk.getId()));
	}
}
