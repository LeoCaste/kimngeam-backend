package com.kimngeam.backend.admin.dashboard;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.kimngeam.backend.shared.entity.Direccion;
import com.kimngeam.backend.shared.entity.Traduccion;
import com.kimngeam.backend.shared.entity.TraduccionRepository;
import com.kimngeam.backend.shared.entity.Usuario;
import com.kimngeam.backend.shared.entity.UsuarioRepository;
import com.kimngeam.backend.shared.security.JwtService;
import com.kimngeam.backend.validaciones.ValidacionService;
import com.kimngeam.backend.validaciones.dto.ValidarExpresionRequest;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * {@code @Transactional}: la validación creada en {@code setUp} (y el
 * corpus_chunk que genera — Ollama, local y gratis) se revierte al final de
 * cada test.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class DashboardControllerTest {

	private static final String BEARER_PREFIX = "Bearer ";
	private static final String EMAIL_ACADEMICO = "tefi@ufro.cl";
	private static final String EMAIL_ADMIN = "admin@kimngeam.cl";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UsuarioRepository usuarioRepository;

	@Autowired
	private TraduccionRepository traduccionRepository;

	@Autowired
	private JwtService jwtService;

	@Autowired
	private ValidacionService validacionService;

	@Test
	void academicoRecibe403() throws Exception {
		mockMvc.perform(get("/admin/dashboard").header(HttpHeaders.AUTHORIZATION, tokenPara(EMAIL_ACADEMICO)))
				.andExpect(status().isForbidden());
	}

	@Test
	void adminObtieneLosAgregados() throws Exception {
		Usuario academico = usuarioRepository.findByEmail(EMAIL_ACADEMICO).orElseThrow();
		Traduccion traduccion = traduccionRepository
				.save(new Traduccion(academico, "origen", "traducido", Direccion.ES_MAP, null, null));
		validacionService.registrarExpresion(
				new ValidarExpresionRequest(traduccion.getId(), "trarilonko",
						"El trarilonko es la cinta ceremonial que usan las mujeres mapuche en la cabeza.",
						"Nguluche"),
				academico.getId());

		mockMvc.perform(get("/admin/dashboard").header(HttpHeaders.AUTHORIZATION, tokenPara(EMAIL_ADMIN)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.total_usuarios").value(Matchers.greaterThanOrEqualTo(2)))
				.andExpect(jsonPath("$.total_traducciones").value(Matchers.greaterThanOrEqualTo(1)))
				.andExpect(jsonPath("$.total_validaciones").value(Matchers.greaterThanOrEqualTo(1)))
				.andExpect(jsonPath("$.expresiones.total").exists())
				.andExpect(jsonPath("$.variantes").isArray())
				.andExpect(jsonPath("$.variantes[0].nombre").exists())
				.andExpect(jsonPath("$.ultimas_validaciones").isArray())
				.andExpect(jsonPath("$.ultimas_validaciones[0].usuario.nombre").exists())
				.andExpect(jsonPath("$.ultimas_validaciones[0].usuario.id").doesNotExist());
	}

	private String tokenPara(String email) {
		Usuario usuario = usuarioRepository.findByEmail(email).orElseThrow();
		return BEARER_PREFIX + jwtService.generarToken(usuario);
	}
}
