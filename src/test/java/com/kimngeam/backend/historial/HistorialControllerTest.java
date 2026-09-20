package com.kimngeam.backend.historial;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.kimngeam.backend.shared.entity.Usuario;
import com.kimngeam.backend.shared.entity.UsuarioRepository;
import com.kimngeam.backend.shared.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Casos de error del contrato para {@code GET /historial}: requiere auth y
 * rol academico (ver docs/API_CONTRACTS.md y CLAUDE.md). Usa los usuarios de
 * {@code DevUsuarioSeeder} (perfil dev, activo en test) para tokens reales.
 */
@SpringBootTest
@AutoConfigureMockMvc
class HistorialControllerTest {

	private static final String BEARER_PREFIX = "Bearer ";
	private static final String EMAIL_ACADEMICO = "tefi@ufro.cl";
	private static final String EMAIL_ADMIN = "admin@kimngeam.cl";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UsuarioRepository usuarioRepository;

	@Autowired
	private JwtService jwtService;

	@Test
	void sinTokenDevuelve401() throws Exception {
		mockMvc.perform(get("/historial"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.error").value(true))
				.andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
	}

	@Test
	void tokenInvalidoDevuelve401() throws Exception {
		mockMvc.perform(get("/historial").header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + "esto-no-es-un-jwt"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
	}

	@Test
	void rolIncorrectoDevuelve403() throws Exception {
		String token = tokenPara(EMAIL_ADMIN);

		mockMvc.perform(get("/historial").header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + token))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.code").value("FORBIDDEN"));
	}

	@Test
	void academicoAutenticadoObtieneSuHistorial() throws Exception {
		String token = tokenPara(EMAIL_ACADEMICO);

		mockMvc.perform(get("/historial").header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.historial").isArray());
	}

	private String tokenPara(String email) {
		Usuario usuario = usuarioRepository.findByEmail(email).orElseThrow();
		return jwtService.generarToken(usuario);
	}
}
