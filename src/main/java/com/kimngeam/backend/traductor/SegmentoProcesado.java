package com.kimngeam.backend.traductor;

import com.kimngeam.backend.rag.generation.ContextoCulturalGenerado;
import com.kimngeam.backend.rag.retrieval.RetrievedCorpusChunk;
import java.math.BigDecimal;
import java.util.List;

/**
 * Resultado intermedio de procesar un segmento: ya generado y con su
 * confianza calculada, pero todavía no persistido. Puente entre
 * {@link TraductorService} (lo produce) y {@link TraduccionPersistor} (lo
 * consume dentro de una sola transacción).
 */
record SegmentoProcesado(int orden, String textoOrigen, String textoTraducido, BigDecimal confianza,
		boolean conRespaldo, List<RetrievedCorpusChunk> chunksUsados, List<ContextoCulturalGenerado> contextoCultural,
		String modeloLlm) {
}
