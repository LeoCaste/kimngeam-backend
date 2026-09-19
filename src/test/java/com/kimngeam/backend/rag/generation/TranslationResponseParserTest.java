package com.kimngeam.backend.rag.generation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

class TranslationResponseParserTest {

	private final TranslationResponseParser parser = new TranslationResponseParser(new ObjectMapper());

	@Test
	void parseaJsonPlano() {
		SegmentTranslationResult resultado = parser.parsear("""
				{"traduccion": "Mari mari", "contexto_cultural": []}
				""");

		assertThat(resultado.traduccion()).isEqualTo("Mari mari");
		assertThat(resultado.contextoCultural()).isEmpty();
	}

	@Test
	void limpiaBloqueDeMarkdownConEtiquetaJson() {
		SegmentTranslationResult resultado = parser.parsear("""
				```json
				{"traduccion": "Mari mari", "contexto_cultural": []}
				```
				""");

		assertThat(resultado.traduccion()).isEqualTo("Mari mari");
	}

	@Test
	void limpiaBloqueDeMarkdownSinEtiqueta() {
		SegmentTranslationResult resultado = parser.parsear("""
				```
				{"traduccion": "Mari mari", "contexto_cultural": []}
				```
				""");

		assertThat(resultado.traduccion()).isEqualTo("Mari mari");
	}

	@Test
	void contextoCulturalAusenteQuedaComoListaVacia() {
		SegmentTranslationResult resultado = parser.parsear("""
				{"traduccion": "Mari mari"}
				""");

		assertThat(resultado.contextoCultural()).isEmpty();
	}

	@Test
	void parseaContextoCulturalConCamposNulos() {
		SegmentTranslationResult resultado = parser.parsear("""
				{
				  "traduccion": "mapuche che",
				  "contexto_cultural": [
				    {"expresion": "mapuche che", "aporte": "gente de la tierra", "comunidad": null, "variante": null}
				  ]
				}
				""");

		assertThat(resultado.contextoCultural()).hasSize(1);
		ContextoCulturalGenerado item = resultado.contextoCultural().get(0);
		assertThat(item.expresion()).isEqualTo("mapuche che");
		assertThat(item.comunidad()).isNull();
	}

	@Test
	void jsonInvalidoLanzaExcepcionDeParsing() {
		assertThatThrownBy(() -> parser.parsear("esto no es json"))
				.isInstanceOf(TranslationParsingException.class);
	}

	@Test
	void traduccionVaciaLanzaExcepcionDeParsing() {
		assertThatThrownBy(() -> parser.parsear("""
				{"traduccion": "", "contexto_cultural": []}
				"""))
				.isInstanceOf(TranslationParsingException.class);
	}

	@Test
	void traduccionAusenteLanzaExcepcionDeParsing() {
		assertThatThrownBy(() -> parser.parsear("""
				{"contexto_cultural": []}
				"""))
				.isInstanceOf(TranslationParsingException.class);
	}
}
