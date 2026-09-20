package com.kimngeam.backend.traductor;

import static org.assertj.core.api.Assertions.assertThat;

import com.kimngeam.backend.rag.retrieval.RetrievedCorpusChunk;
import com.kimngeam.backend.shared.entity.SourceType;
import com.kimngeam.backend.traductor.ConfianzaCalculator.SegmentoConfianza;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ConfianzaCalculatorTest {

	// techo=0.72, umbral=0.5 (default de TraductorProperties), peso-maximo=0.5,
	// chunks-para-cobertura-completa=3 — ver ConfianzaProperties para el porqué.
	private final ConfianzaCalculator confianzaCalculator = new ConfianzaCalculator(
			new ConfianzaProperties(new BigDecimal("1.5"), 0.72, 0.5, 3), new TraductorProperties(0.5));

	@Test
	void sinChunksNoHayConfianza() {
		BigDecimal confianza = confianzaCalculator.calcularSegmento(List.of());

		assertThat(confianza).isNull();
	}

	@Test
	void unSoloChunkFuerteSinCoberturaQuedaSaturadoPorCantidadDeChunks() {
		// combinado = max = promedio = 0.80; reescalado = (0.80-0.5)/(0.72-0.5) =
		// 1.36 -> clamp 1.0; cobertura = min(1, 1/3) = 0.3333; 1.0*0.3333 = 0.33.
		// Un solo chunk, por fuerte que sea, no basta para "cobertura completa" —
		// hacen falta 3 chunks concordantes (ver ConfianzaProperties).
		BigDecimal confianza = confianzaCalculator.calcularSegmento(List.of(chunk(0.80, false)));

		assertThat(confianza).isEqualByComparingTo("0.33");
	}

	@Test
	void unChunkValidadoPesaMasQueUnoSinValidar() {
		// promedioPonderado = (0.60*1 + 0.90*1.5) / (1 + 1.5) = 0.78; max = 0.90
		// combinado = 0.5*0.90 + 0.5*0.78 = 0.84
		// reescalado = (0.84-0.5)/(0.72-0.5) = 1.545 -> clamp 1.0
		// cobertura = min(1, 2/3) = 0.6667; 1.0*0.6667 = 0.67
		BigDecimal confianza = confianzaCalculator.calcularSegmento(List.of(chunk(0.60, false), chunk(0.90, true)));

		assertThat(confianza).isEqualByComparingTo("0.67");
	}

	@Test
	void tresChunksConScoreCercaDelUmbralDanConfianzaBaja() {
		// promedio = max = 0.51 (los tres iguales); combinado = 0.51
		// reescalado = (0.51-0.5)/(0.72-0.5) = 0.0455; cobertura = min(1, 3/3) = 1.0
		BigDecimal confianza = confianzaCalculator
				.calcularSegmento(List.of(chunk(0.51, false), chunk(0.51, false), chunk(0.51, false)));

		assertThat(confianza).isEqualByComparingTo("0.05");
	}

	@Test
	void globalEsElPromedioPonderadoPorLargoDeSegmento() {
		// (0.80*10 + 0.40*30) / (10 + 30) = 20.0 / 40 = 0.50
		BigDecimal confianza = confianzaCalculator
				.calcularGlobal(List.of(new SegmentoConfianza(new BigDecimal("0.80"), 10),
						new SegmentoConfianza(new BigDecimal("0.40"), 30)));

		assertThat(confianza).isEqualByComparingTo("0.50");
	}

	@Test
	void segmentoSinRespaldoAportaCeroPeroSuLargoCuentaEnElDenominador() {
		// (0.90*10 + 0*10) / (10 + 10) = 9.0 / 20 = 0.45
		BigDecimal confianza = confianzaCalculator
				.calcularGlobal(List.of(new SegmentoConfianza(new BigDecimal("0.90"), 10),
						new SegmentoConfianza(null, 10)));

		assertThat(confianza).isEqualByComparingTo("0.45");
	}

	@Test
	void todosLosSegmentosSinRespaldoDanConfianzaCero() {
		BigDecimal confianza = confianzaCalculator
				.calcularGlobal(List.of(new SegmentoConfianza(null, 10), new SegmentoConfianza(null, 20)));

		assertThat(confianza).isEqualByComparingTo("0.00");
	}

	private static RetrievedCorpusChunk chunk(double similitud, boolean validado) {
		return new RetrievedCorpusChunk(UUID.randomUUID(), SourceType.TRANSLATION_EXAMPLES, "ref", "contenido", null,
				similitud, validado);
	}
}
