package com.kimngeam.backend.traductor;

import com.kimngeam.backend.rag.generation.ContextoCulturalGenerado;
import com.kimngeam.backend.rag.retrieval.RetrievedCorpusChunk;
import com.kimngeam.backend.shared.entity.ContextoCultural;
import com.kimngeam.backend.shared.entity.ContextoCulturalRepository;
import com.kimngeam.backend.shared.entity.CorpusChunk;
import com.kimngeam.backend.shared.entity.CorpusChunkRepository;
import com.kimngeam.backend.shared.entity.Direccion;
import com.kimngeam.backend.shared.entity.Traduccion;
import com.kimngeam.backend.shared.entity.TraduccionFuente;
import com.kimngeam.backend.shared.entity.TraduccionFuenteRepository;
import com.kimngeam.backend.shared.entity.TraduccionRepository;
import com.kimngeam.backend.shared.entity.TraduccionSegmento;
import com.kimngeam.backend.shared.entity.TraduccionSegmentoRepository;
import com.kimngeam.backend.shared.entity.Usuario;
import com.kimngeam.backend.shared.entity.UsuarioRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persiste una traducción ya generada — segmentos, fuentes y contexto
 * cultural incluidos — en una sola transacción. Separado de
 * {@link TraductorService} a propósito: ese servicio hace llamadas de red
 * lentas (retrieval, LLM) que no deben mantener una transacción de base de
 * datos abierta; acá solo entra trabajo ya calculado en memoria.
 */
@Service
public class TraduccionPersistor {

	private static final int ESCALA_SCORE = 4;

	private final TraduccionRepository traduccionRepository;
	private final TraduccionSegmentoRepository segmentoRepository;
	private final TraduccionFuenteRepository fuenteRepository;
	private final ContextoCulturalRepository contextoCulturalRepository;
	private final UsuarioRepository usuarioRepository;
	private final CorpusChunkRepository corpusChunkRepository;

	public TraduccionPersistor(TraduccionRepository traduccionRepository,
			TraduccionSegmentoRepository segmentoRepository, TraduccionFuenteRepository fuenteRepository,
			ContextoCulturalRepository contextoCulturalRepository, UsuarioRepository usuarioRepository,
			CorpusChunkRepository corpusChunkRepository) {
		this.traduccionRepository = traduccionRepository;
		this.segmentoRepository = segmentoRepository;
		this.fuenteRepository = fuenteRepository;
		this.contextoCulturalRepository = contextoCulturalRepository;
		this.usuarioRepository = usuarioRepository;
		this.corpusChunkRepository = corpusChunkRepository;
	}

	@Transactional
	public TraduccionResultado persistir(Long usuarioId, String textoOrigen, String textoTraducido,
			Direccion direccion, BigDecimal confianzaGlobal, String modeloLlm, List<SegmentoProcesado> segmentos) {
		Traduccion traduccion = traduccionRepository
				.save(new Traduccion(referenciaUsuario(usuarioId), textoOrigen, textoTraducido, direccion,
						confianzaGlobal, modeloLlm));

		List<ContextoCulturalResultado> contextos = new ArrayList<>();
		for (SegmentoProcesado segmentoProcesado : segmentos) {
			TraduccionSegmento segmento = persistirSegmento(traduccion, segmentoProcesado);
			persistirFuentes(segmento, segmentoProcesado.chunksUsados());
			if (direccion == Direccion.ES_MAP) {
				contextos.addAll(persistirContextoCultural(traduccion, segmentoProcesado.contextoCultural()));
			}
		}

		return new TraduccionResultado(traduccion.getId(), textoOrigen, textoTraducido, direccion, confianzaGlobal,
				contextos, traduccion.getFecha());
	}

	private Usuario referenciaUsuario(Long usuarioId) {
		return usuarioId == null ? null : usuarioRepository.getReferenceById(usuarioId);
	}

	private TraduccionSegmento persistirSegmento(Traduccion traduccion, SegmentoProcesado segmentoProcesado) {
		return segmentoRepository.save(new TraduccionSegmento(traduccion, segmentoProcesado.orden(),
				segmentoProcesado.textoOrigen(), segmentoProcesado.textoTraducido(), segmentoProcesado.confianza(),
				segmentoProcesado.conRespaldo()));
	}

	private void persistirFuentes(TraduccionSegmento segmento, List<RetrievedCorpusChunk> chunksUsados) {
		for (RetrievedCorpusChunk chunk : chunksUsados) {
			CorpusChunk corpusChunkRef = corpusChunkRepository.getReferenceById(chunk.id());
			BigDecimal score = BigDecimal.valueOf(chunk.similitud()).setScale(ESCALA_SCORE, RoundingMode.HALF_UP);
			fuenteRepository.save(new TraduccionFuente(segmento, corpusChunkRef, score));
		}
	}

	private List<ContextoCulturalResultado> persistirContextoCultural(Traduccion traduccion,
			List<ContextoCulturalGenerado> items) {
		List<ContextoCulturalResultado> guardados = new ArrayList<>();
		for (ContextoCulturalGenerado item : items) {
			contextoCulturalRepository
					.save(new ContextoCultural(traduccion, item.expresion(), item.aporte(), item.comunidad(),
							item.variante()));
			guardados.add(new ContextoCulturalResultado(item.expresion(), item.aporte(), item.comunidad(),
					item.variante()));
		}
		return guardados;
	}
}
