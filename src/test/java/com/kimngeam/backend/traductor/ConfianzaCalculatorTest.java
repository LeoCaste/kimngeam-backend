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

	private final ConfianzaCalculator confianzaCalculator = new ConfianzaCalculator(
			new ConfianzaProperties(new BigDecimal("1.5")));

	@Test
	void sinChunksNoHayConfianza() {
		BigDecimal confianza = confianzaCalculator.calcularSegmento(List.of());

		assertThat(confianza).isNull();
	}

	@Test
	void unSoloChunkSinValidarUsaSuPropioScore() {
		BigDecimal confianza = confianzaCalculator.calcularSegmento(List.of(chunk(0.80, false)));

		assertThat(confianza).isEqualByComparingTo("0.80");
	}

	@Test
	void unChunkValidadoPesaMasQueUnoSinValidar() {
		// (0.60*1 + 0.90*1.5) / (1 + 1.5) = 1.95 / 2.5 = 0.78
		BigDecimal confianza = confianzaCalculator.calcularSegmento(List.of(chunk(0.60, false), chunk(0.90, true)));

		assertThat(confianza).isEqualByComparingTo("0.78");
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
