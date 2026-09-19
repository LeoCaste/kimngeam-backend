package com.kimngeam.backend.traductor;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class TextSegmenterTest {

	private final TextSegmenter textSegmenter = new TextSegmenter();

	@Test
	void textoBajoElUmbralQuedaComoUnSoloSegmento() {
		List<String> segmentos = textSegmenter.segmentar("Gracias.", 200);

		assertThat(segmentos).containsExactly("Gracias.");
	}

	@Test
	void textoSobreElUmbralSePartePorOraciones() {
		String texto = "El pueblo mapuche habita desde tiempos ancestrales. Su lengua es el mapudungun. "
				+ "Hoy se sigue transmitiendo de generación en generación.";

		List<String> segmentos = textSegmenter.segmentar(texto, 10);

		assertThat(segmentos).containsExactly("El pueblo mapuche habita desde tiempos ancestrales.",
				"Su lengua es el mapudungun.", "Hoy se sigue transmitiendo de generación en generación.");
	}

	@Test
	void textoExactoAlUmbralNoSeSegmenta() {
		String texto = "1234567890";

		List<String> segmentos = textSegmenter.segmentar(texto, 10);

		assertThat(segmentos).containsExactly(texto);
	}

	@Test
	void recortaEspaciosAlPrincipioYAlFinal() {
		List<String> segmentos = textSegmenter.segmentar("   Gracias.   ", 200);

		assertThat(segmentos).containsExactly("Gracias.");
	}
}
