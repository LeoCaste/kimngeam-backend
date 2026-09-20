package com.kimngeam.backend.traductor;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Casos de error del contrato para {@code POST /traductor/traducir} (ver
 * docs/API_CONTRACTS.md). Ninguno de estos dos casos llega a tocar retrieval
 * ni al LLM: {@code TraductorService.traducir(...)} valida el texto antes de
 * cualquier llamada de red, y la dirección se valida en el binding del
 * request (@Pattern) — así que no cuestan nada y corren en el build normal.
 */
@SpringBootTest
@AutoConfigureMockMvc
class TraductorControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private SegmentacionProperties segmentacionProperties;

	@Test
	void textoQueSuperaElMaximoDevuelve400() throws Exception {
		String textoLargo = "a".repeat(segmentacionProperties.maximoEntrada() + 1);
		String body = """
				{"texto": "%s", "direccion": "es-map"}
				""".formatted(textoLargo);

		mockMvc.perform(post("/traductor/traducir").contentType(MediaType.APPLICATION_JSON).content(body))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error").value(true))
				.andExpect(jsonPath("$.code").value("TEXTO_DEMASIADO_LARGO"));
	}

	@Test
	void direccionInvalidaDevuelve400() throws Exception {
		String body = """
				{"texto": "hola", "direccion": "invalida"}
				""";

		mockMvc.perform(post("/traductor/traducir").contentType(MediaType.APPLICATION_JSON).content(body))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error").value(true))
				.andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
	}

	@Test
	void textoVacioDevuelve400() throws Exception {
		String body = """
				{"texto": "", "direccion": "es-map"}
				""";

		mockMvc.perform(post("/traductor/traducir").contentType(MediaType.APPLICATION_JSON).content(body))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error").value(true))
				.andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
	}
}
