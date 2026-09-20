package com.kimngeam.backend.shared.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Confirma que {@code CorsConfig} solo deja pasar el origen explícito de
 * {@code CorsProperties} (default de test: {@code http://localhost:5173}) —
 * nunca {@code "*"}, la API maneja tokens JWT (ver CLAUDE.md, Fase 6).
 */
@SpringBootTest
@AutoConfigureMockMvc
class CorsConfigurationTest {

	private static final String ORIGEN_PERMITIDO = "http://localhost:5173";
	private static final String ORIGEN_NO_PERMITIDO = "https://origen-no-autorizado.cl";

	@Autowired
	private MockMvc mockMvc;

	@Test
	void preflightDesdeOrigenPermitidoDevuelveElHeaderDeCors() throws Exception {
		mockMvc.perform(options("/auth/login")
				.header(HttpHeaders.ORIGIN, ORIGEN_PERMITIDO)
				.header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST"))
				.andExpect(status().isOk())
				.andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, ORIGEN_PERMITIDO));
	}

	@Test
	void preflightDesdeOrigenNoPermitidoEsRechazado() throws Exception {
		mockMvc.perform(options("/auth/login")
				.header(HttpHeaders.ORIGIN, ORIGEN_NO_PERMITIDO)
				.header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST"))
				.andExpect(status().isForbidden())
				.andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
	}
}
