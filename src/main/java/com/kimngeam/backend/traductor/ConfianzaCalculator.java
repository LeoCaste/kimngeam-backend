package com.kimngeam.backend.traductor;

import com.kimngeam.backend.rag.retrieval.RetrievedCorpusChunk;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Deriva la confianza de una traducción exclusivamente de los scores del
 * retrieval — nunca se le pide al LLM que la invente (ver CLAUDE.md, Fase 3).
 */
@Component
public class ConfianzaCalculator {

	private static final BigDecimal PESO_BASE = BigDecimal.ONE;
	private static final int ESCALA = 2;

	private final ConfianzaProperties properties;

	public ConfianzaCalculator(ConfianzaProperties properties) {
		this.properties = properties;
	}

	/**
	 * Confianza de un segmento: promedio ponderado de la similitud de los
	 * chunks que lo respaldaron (ya filtrados por
	 * {@code kimngeam.traductor.umbral-similitud}). Un chunk validado por un
	 * académico ({@code corpus_chunk.validado = true}) pesa
	 * {@code kimngeam.traductor.confianza.peso-chunk-validado} veces más que
	 * uno sin revisar (peso 1) — una fuente curada es más confiable que una
	 * transcripción cruda. {@code null} si no hay chunks que la respalden: no
	 * hay base para derivar un número, no es lo mismo que "confianza cero".
	 */
	public BigDecimal calcularSegmento(List<RetrievedCorpusChunk> chunksUsados) {
		if (chunksUsados.isEmpty()) {
			return null;
		}
		BigDecimal sumaPonderada = BigDecimal.ZERO;
		BigDecimal sumaPesos = BigDecimal.ZERO;
		for (RetrievedCorpusChunk chunk : chunksUsados) {
			BigDecimal peso = chunk.validado() ? properties.pesoChunkValidado() : PESO_BASE;
			sumaPonderada = sumaPonderada.add(peso.multiply(BigDecimal.valueOf(chunk.similitud())));
			sumaPesos = sumaPesos.add(peso);
		}
		return sumaPonderada.divide(sumaPesos, ESCALA, RoundingMode.HALF_UP);
	}

	/**
	 * Confianza global de la traducción: promedio de las confianzas de sus
	 * segmentos, ponderado por el largo en caracteres del texto original de
	 * cada uno — un segmento largo pesa más en el resultado final que uno
	 * corto. Un segmento sin confianza (sin respaldo del corpus) aporta 0 al
	 * numerador pero su largo sigue contando en el denominador, así que
	 * arrastra la confianza global hacia abajo en vez de ignorarse: una
	 * traducción con partes sin respaldo debe reflejarlo en su número final.
	 */
	public BigDecimal calcularGlobal(List<SegmentoConfianza> segmentos) {
		BigDecimal numerador = BigDecimal.ZERO;
		BigDecimal denominador = BigDecimal.ZERO;
		for (SegmentoConfianza segmento : segmentos) {
			BigDecimal peso = BigDecimal.valueOf(segmento.longitud());
			BigDecimal confianza = segmento.confianza() == null ? BigDecimal.ZERO : segmento.confianza();
			numerador = numerador.add(confianza.multiply(peso));
			denominador = denominador.add(peso);
		}
		if (denominador.signum() == 0) {
			return BigDecimal.ZERO.setScale(ESCALA, RoundingMode.UNNECESSARY);
		}
		return numerador.divide(denominador, ESCALA, RoundingMode.HALF_UP);
	}

	public record SegmentoConfianza(BigDecimal confianza, int longitud) {
	}
}
