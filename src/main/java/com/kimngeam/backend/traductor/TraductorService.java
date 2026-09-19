package com.kimngeam.backend.traductor;

import com.kimngeam.backend.rag.generation.SegmentTranslationOutcome;
import com.kimngeam.backend.rag.generation.SegmentTranslator;
import com.kimngeam.backend.rag.generation.TranslationPromptBuilder;
import com.kimngeam.backend.rag.retrieval.CorpusRetrievalService;
import com.kimngeam.backend.rag.retrieval.RetrievedCorpusChunk;
import com.kimngeam.backend.shared.entity.Direccion;
import com.kimngeam.backend.shared.error.BadRequestException;
import com.kimngeam.backend.traductor.ConfianzaCalculator.SegmentoConfianza;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

/**
 * Orquesta una traducción completa: segmentar → por cada segmento, retrieval
 * + generación con el segmento anterior como contexto → confianza derivada
 * del retrieval → persistencia atómica (ver {@link TraduccionPersistor}). No
 * abre transacción de base de datos acá a propósito: las llamadas de red
 * (retrieval, LLM) pueden tardar hasta 90s cada una (ver CLAUDE.md, Fase 3) y
 * no deben mantener una conexión de base de datos retenida todo ese tiempo.
 */
@Service
public class TraductorService {

	private final TextSegmenter textSegmenter;
	private final SegmentacionProperties segmentacionProperties;
	private final TraductorProperties traductorProperties;
	private final CorpusRetrievalService corpusRetrievalService;
	private final TranslationPromptBuilder promptBuilder;
	private final SegmentTranslator segmentTranslator;
	private final ConfianzaCalculator confianzaCalculator;
	private final TraduccionPersistor traduccionPersistor;

	public TraductorService(TextSegmenter textSegmenter, SegmentacionProperties segmentacionProperties,
			TraductorProperties traductorProperties, CorpusRetrievalService corpusRetrievalService,
			TranslationPromptBuilder promptBuilder, SegmentTranslator segmentTranslator,
			ConfianzaCalculator confianzaCalculator, TraduccionPersistor traduccionPersistor) {
		this.textSegmenter = textSegmenter;
		this.segmentacionProperties = segmentacionProperties;
		this.traductorProperties = traductorProperties;
		this.corpusRetrievalService = corpusRetrievalService;
		this.promptBuilder = promptBuilder;
		this.segmentTranslator = segmentTranslator;
		this.confianzaCalculator = confianzaCalculator;
		this.traduccionPersistor = traduccionPersistor;
	}

	public TraduccionResultado traducir(String textoOrigen, Direccion direccion, Long usuarioId) {
		validar(textoOrigen);

		List<String> textosSegmentos = textSegmenter.segmentar(textoOrigen, segmentacionProperties.umbralSegmentacion());
		List<SegmentoProcesado> segmentosProcesados = procesarSegmentos(textosSegmentos, direccion);

		String textoTraducido = segmentosProcesados.stream()
				.map(SegmentoProcesado::textoTraducido)
				.collect(Collectors.joining(" "));
		BigDecimal confianzaGlobal = confianzaCalculator.calcularGlobal(segmentosProcesados.stream()
				.map(segmento -> new SegmentoConfianza(segmento.confianza(), segmento.textoOrigen().length()))
				.toList());
		String modeloLlm = segmentosProcesados.get(0).modeloLlm();

		return traduccionPersistor.persistir(usuarioId, textoOrigen, textoTraducido, direccion, confianzaGlobal,
				modeloLlm, segmentosProcesados);
	}

	private List<SegmentoProcesado> procesarSegmentos(List<String> textosSegmentos, Direccion direccion) {
		List<SegmentoProcesado> segmentosProcesados = new ArrayList<>(textosSegmentos.size());
		String textoAnterior = null;
		String traduccionAnterior = null;
		for (int i = 0; i < textosSegmentos.size(); i++) {
			SegmentoProcesado procesado = procesarSegmento(i + 1, textosSegmentos.get(i), direccion, textoAnterior,
					traduccionAnterior);
			segmentosProcesados.add(procesado);
			textoAnterior = procesado.textoOrigen();
			traduccionAnterior = procesado.textoTraducido();
		}
		return segmentosProcesados;
	}

	private SegmentoProcesado procesarSegmento(int orden, String textoSegmento, Direccion direccion,
			String textoAnterior, String traduccionAnterior) {
		List<RetrievedCorpusChunk> chunksUsados = corpusRetrievalService.buscar(textoSegmento)
				.stream()
				.filter(chunk -> chunk.similitud() >= traductorProperties.umbralSimilitud())
				.toList();
		boolean conRespaldo = !chunksUsados.isEmpty();

		String prompt = promptBuilder.construir(direccion, textoSegmento, textoAnterior, traduccionAnterior,
				chunksUsados);
		SegmentTranslationOutcome outcome = segmentTranslator.generar(prompt);

		BigDecimal confianza = conRespaldo ? confianzaCalculator.calcularSegmento(chunksUsados) : null;

		return new SegmentoProcesado(orden, textoSegmento, outcome.resultado().traduccion(), confianza, conRespaldo,
				chunksUsados, outcome.resultado().contextoCultural(), outcome.modeloLlm());
	}

	private void validar(String textoOrigen) {
		if (textoOrigen == null || textoOrigen.isBlank()) {
			throw new BadRequestException("El texto a traducir no puede estar vacío");
		}
		if (textoOrigen.length() > segmentacionProperties.maximoEntrada()) {
			throw new BadRequestException("TEXTO_DEMASIADO_LARGO", "El texto tiene " + textoOrigen.length()
					+ " caracteres; el máximo permitido es " + segmentacionProperties.maximoEntrada());
		}
	}
}
