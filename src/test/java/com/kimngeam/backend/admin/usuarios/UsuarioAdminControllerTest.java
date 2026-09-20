package com.kimngeam.backend.admin.usuarios;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Casos de error del contrato para {@code /admin/usuarios/**}, más los casos
 * felices — ninguno hace llamadas de red reales (invitar solo loguea con el
 * proveedor de correo default), así que corre en el build normal.
 * {@code @Transactional} para que la invitación creada en el test se revierta.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UsuarioAdminControllerTest {

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
	void academicoRecibe403() throws Exception {
		mockMvc.perform(get("/admin/usuarios").header(HttpHeaders.AUTHORIZATION, tokenPara(EMAIL_ACADEMICO)))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.code").value("FORBIDDEN"));
	}

	@Test
	void sinTokenRecibe401() throws Exception {
		mockMvc.perform(get("/admin/usuarios"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
	}

	@Test
	void adminListaUsuarios() throws Exception {
		mockMvc.perform(get("/admin/usuarios").header(HttpHeaders.AUTHORIZATION, tokenPara(EMAIL_ADMIN)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.usuarios").isArray())
				.andExpect(jsonPath("$.total").value(org.hamcrest.Matchers.greaterThanOrEqualTo(2)));
	}

	@Test
	void adminObtieneDetalleDeUnUsuario() throws Exception {
		Long academicoId = usuarioRepository.findByEmail(EMAIL_ACADEMICO).orElseThrow().getId();

		mockMvc.perform(get("/admin/usuarios/" + academicoId).header(HttpHeaders.AUTHORIZATION,
				tokenPara(EMAIL_ADMIN)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.usuario.email").value(EMAIL_ACADEMICO))
				.andExpect(jsonPath("$.validaciones_recientes").isArray());
	}

	@Test
	void adminObtieneDetalleDeUsuarioInexistenteRecibe404() throws Exception {
		mockMvc.perform(get("/admin/usuarios/999999").header(HttpHeaders.AUTHORIZATION, tokenPara(EMAIL_ADMIN)))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.code").value("NOT_FOUND"));
	}

	@Test
	void adminInvitaUnAcademicoNuevo() throws Exception {
		String body = """
				{"nombre": "Stephanie Nueva", "email": "stephanie.nueva.test@ufro.cl"}
				""";

		mockMvc.perform(post("/admin/usuarios/invitar").contentType(MediaType.APPLICATION_JSON)
				.header(HttpHeaders.AUTHORIZATION, tokenPara(EMAIL_ADMIN))
				.content(body))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.message").value("Invitación enviada a stephanie.nueva.test@ufro.cl"));
	}

	@Test
	void adminInvitaAUnEmailYaRegistradoRecibe409() throws Exception {
		String body = """
				{"nombre": "Tefi Duplicada", "email": "%s"}
				""".formatted(EMAIL_ACADEMICO);

		mockMvc.perform(post("/admin/usuarios/invitar").contentType(MediaType.APPLICATION_JSON)
				.header(HttpHeaders.AUTHORIZATION, tokenPara(EMAIL_ADMIN))
				.content(body))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("EMAIL_TAKEN"));
	}

	@Test
	void adminInvitaSinEmailRecibe400() throws Exception {
		String body = """
				{"nombre": "Sin Email"}
				""";

		mockMvc.perform(post("/admin/usuarios/invitar").contentType(MediaType.APPLICATION_JSON)
				.header(HttpHeaders.AUTHORIZATION, tokenPara(EMAIL_ADMIN))
				.content(body))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
	}

	@Test
	void adminCambiaEstadoDeUnUsuario() throws Exception {
		Long academicoId = usuarioRepository.findByEmail(EMAIL_ACADEMICO).orElseThrow().getId();

		mockMvc.perform(patch("/admin/usuarios/" + academicoId + "/estado").contentType(MediaType.APPLICATION_JSON)
				.header(HttpHeaders.AUTHORIZATION, tokenPara(EMAIL_ADMIN))
				.content("{\"estado\": \"inactivo\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.message").value("Estado actualizado correctamente"));
	}

	@Test
	void adminNoPuedeAsignarSinActividadRecibe400() throws Exception {
		Long academicoId = usuarioRepository.findByEmail(EMAIL_ACADEMICO).orElseThrow().getId();

		mockMvc.perform(patch("/admin/usuarios/" + academicoId + "/estado").contentType(MediaType.APPLICATION_JSON)
				.header(HttpHeaders.AUTHORIZATION, tokenPara(EMAIL_ADMIN))
				.content("{\"estado\": \"sin-actividad\"}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
	}

	private String tokenPara(String email) {
		Usuario usuario = usuarioRepository.findByEmail(email).orElseThrow();
		return BEARER_PREFIX + jwtService.generarToken(usuario);
	}
}
