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
	private static final int ESCALA_INTERNA = 10;

	private final ConfianzaProperties properties;
	private final TraductorProperties traductorProperties;

	public ConfianzaCalculator(ConfianzaProperties properties, TraductorProperties traductorProperties) {
		this.properties = properties;
		this.traductorProperties = traductorProperties;
	}

	/**
	 * Confianza de un segmento, sobre los chunks que lo respaldaron (ya
	 * filtrados por {@code kimngeam.traductor.umbral-similitud}), en tres
	 * pasos — ver {@link ConfianzaProperties} para el porqué de cada uno:
	 * <ol>
	 * <li>Combina el score máximo con el promedio ponderado (un chunk
	 * validado por un académico pesa {@code peso-chunk-validado} veces más
	 * que uno sin revisar) — el promedio solo diluye un match fuerte entre
	 * varios mediocres, así que {@code peso-maximo} decide cuánto pesa ese
	 * pico frente al conjunto.</li>
	 * <li>Reescala ese score combinado contra el rango real
	 * [{@code umbral-similitud}, {@code techo-similitud}] en vez de
	 * [0, 1].</li>
	 * <li>Aplica un factor de cobertura que satura en 1.0 al llegar a
	 * {@code chunks-para-cobertura-completa} chunks de respaldo — varias
	 * fuentes concordantes son mejor evidencia que una sola, aunque su score
	 * individual sea parecido.</li>
	 * </ol>
	 * {@code null} si no hay chunks que la respalden: no hay base para
	 * derivar un número, no es lo mismo que "confianza cero".
	 */
	public BigDecimal calcularSegmento(List<RetrievedCorpusChunk> chunksUsados) {
		if (chunksUsados.isEmpty()) {
			return null;
		}

		BigDecimal sumaPonderada = BigDecimal.ZERO;
		BigDecimal sumaPesos = BigDecimal.ZERO;
		double maximo = 0;
		for (RetrievedCorpusChunk chunk : chunksUsados) {
			BigDecimal peso = chunk.validado() ? properties.pesoChunkValidado() : PESO_BASE;
			sumaPonderada = sumaPonderada.add(peso.multiply(BigDecimal.valueOf(chunk.similitud())));
			sumaPesos = sumaPesos.add(peso);
			maximo = Math.max(maximo, chunk.similitud());
		}
		BigDecimal promedioPonderado = sumaPonderada.divide(sumaPesos, ESCALA_INTERNA, RoundingMode.HALF_UP);

		BigDecimal pesoMaximo = BigDecimal.valueOf(properties.pesoMaximo());
		BigDecimal combinado = BigDecimal.valueOf(maximo).multiply(pesoMaximo)
				.add(promedioPonderado.multiply(BigDecimal.ONE.subtract(pesoMaximo)));

		BigDecimal umbral = BigDecimal.valueOf(traductorProperties.umbralSimilitud());
		BigDecimal techo = BigDecimal.valueOf(properties.techoSimilitud());
		BigDecimal reescalado = clampUnitario(
				combinado.subtract(umbral).divide(techo.subtract(umbral), ESCALA_INTERNA, RoundingMode.HALF_UP));

		double factorCobertura = Math.min(1.0,
				chunksUsados.size() / (double) properties.chunksParaCoberturaCompleta());

		return reescalado.multiply(BigDecimal.valueOf(factorCobertura)).setScale(ESCALA, RoundingMode.HALF_UP);
	}

	private static BigDecimal clampUnitario(BigDecimal valor) {
		if (valor.compareTo(BigDecimal.ZERO) < 0) {
			return BigDecimal.ZERO;
		}
		if (valor.compareTo(BigDecimal.ONE) > 0) {
			return BigDecimal.ONE;
		}
		return valor;
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
