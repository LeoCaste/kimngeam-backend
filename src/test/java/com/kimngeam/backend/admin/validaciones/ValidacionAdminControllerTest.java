package com.kimngeam.backend.admin.validaciones;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.kimngeam.backend.shared.entity.Direccion;
import com.kimngeam.backend.shared.entity.Traduccion;
import com.kimngeam.backend.shared.entity.TraduccionRepository;
import com.kimngeam.backend.shared.entity.Usuario;
import com.kimngeam.backend.shared.entity.UsuarioRepository;
import com.kimngeam.backend.shared.security.JwtService;
import com.kimngeam.backend.validaciones.ValidacionService;
import com.kimngeam.backend.validaciones.dto.ValidacionResponse;
import com.kimngeam.backend.validaciones.dto.ValidarExpresionRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * {@code @Transactional}: la validación creada en {@code setUp} (y el
 * corpus_chunk que genera — Ollama, local y gratis, ver
 * {@code ValidacionCorpusIndexer}) se revierten al final de cada test.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ValidacionAdminControllerTest {

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
		mockMvc.perform(get("/admin/validaciones").header(HttpHeaders.AUTHORIZATION, tokenPara(EMAIL_ACADEMICO)))
				.andExpect(status().isForbidden());
	}

	@Test
	void adminListaValidacionesConFiltroDeTipo() throws Exception {
		crearValidacionDeExpresion();

		mockMvc.perform(get("/admin/validaciones?tipo=expresion").header(HttpHeaders.AUTHORIZATION,
				tokenPara(EMAIL_ADMIN)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.validaciones").isArray())
				.andExpect(jsonPath("$.validaciones[0].tipo").value("expresion"))
				.andExpect(jsonPath("$.validaciones[0].usuario.id").exists());
	}

	@Test
	void adminListaValidacionesConTipoInvalidoRecibe400() throws Exception {
		mockMvc.perform(get("/admin/validaciones?tipo=algo-raro").header(HttpHeaders.AUTHORIZATION,
				tokenPara(EMAIL_ADMIN)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
	}

	@Test
	void adminListaValidacionesConBusqueda() throws Exception {
		crearValidacionDeExpresion();

		mockMvc.perform(get("/admin/validaciones?busqueda=trarilonko").header(HttpHeaders.AUTHORIZATION,
				tokenPara(EMAIL_ADMIN)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.total").value(org.hamcrest.Matchers.greaterThanOrEqualTo(1)));
	}

	@Test
	void adminRevierteUnaValidacion() throws Exception {
		ValidacionResponse creada = crearValidacionDeExpresion();

		mockMvc.perform(post("/admin/validaciones/" + creada.id() + "/revertir").header(HttpHeaders.AUTHORIZATION,
				tokenPara(EMAIL_ADMIN)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.message").value("Validación revertida correctamente"));
	}

	@Test
	void adminRevierteUnaValidacionInexistenteRecibe404() throws Exception {
		mockMvc.perform(post("/admin/validaciones/val_no-existe/revertir").header(HttpHeaders.AUTHORIZATION,
				tokenPara(EMAIL_ADMIN)))
				.andExpect(status().isNotFound());
	}

	private ValidacionResponse crearValidacionDeExpresion() {
		Usuario academico = usuarioRepository.findByEmail(EMAIL_ACADEMICO).orElseThrow();
		Traduccion traduccion = traduccionRepository
				.save(new Traduccion(academico, "origen", "traducido", Direccion.ES_MAP, null, null));
		ValidarExpresionRequest request = new ValidarExpresionRequest(traduccion.getId(), "trarilonko",
				"El trarilonko es la cinta ceremonial que usan las mujeres mapuche en la cabeza.", "Nguluche");
		return validacionService.registrarExpresion(request, academico.getId());
	}

	private String tokenPara(String email) {
		Usuario usuario = usuarioRepository.findByEmail(email).orElseThrow();
		return BEARER_PREFIX + jwtService.generarToken(usuario);
	}
}
