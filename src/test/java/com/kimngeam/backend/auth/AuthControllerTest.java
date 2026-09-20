package com.kimngeam.backend.auth;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
 * Casos de error y felices del contrato para los 4 endpoints de {@code /auth}
 * (ver docs/API_CONTRACTS.md y CLAUDE.md, "Fase 6"). Usa los usuarios de
 * {@code DevUsuarioSeeder} (perfil dev, activo en test) para credenciales
 * reales. {@code @Transactional} porque login/login-admin actualizan
 * {@code ultimo_acceso} y registro/logout insertan filas nuevas.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthControllerTest {

	private static final String BEARER_PREFIX = "Bearer ";
	private static final String EMAIL_ACADEMICO = "tefi@ufro.cl";
	private static final String PASSWORD_ACADEMICO = "password123";
	private static final String EMAIL_ADMIN = "admin@kimngeam.cl";
	private static final String PASSWORD_ADMIN = "admin1234";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UsuarioRepository usuarioRepository;

	@Autowired
	private JwtService jwtService;

	@Test
	void loginConCredencialesValidasDevuelveTokenYUsuario() throws Exception {
		String body = """
				{"email": "%s", "password": "%s"}
				""".formatted(EMAIL_ACADEMICO, PASSWORD_ACADEMICO);

		mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content(body))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.token").isNotEmpty())
				.andExpect(jsonPath("$.usuario.email").value(EMAIL_ACADEMICO))
				.andExpect(jsonPath("$.usuario.rol").value("academico"));
	}

	@Test
	void loginConPasswordIncorrectaDevuelve401() throws Exception {
		String body = """
				{"email": "%s", "password": "password-incorrecta"}
				""".formatted(EMAIL_ACADEMICO);

		mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content(body))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.error").value(true))
				.andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
	}

	@Test
	void loginConEmailInexistenteDevuelve401() throws Exception {
		String body = """
				{"email": "no-existe@ufro.cl", "password": "cualquiera123"}
				""";

		mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content(body))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
	}

	@Test
	void loginSinPasswordDevuelve400() throws Exception {
		String body = """
				{"email": "%s"}
				""".formatted(EMAIL_ACADEMICO);

		mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content(body))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
	}

	@Test
	void loginAdminConCredencialesDeAdminDevuelveToken() throws Exception {
		String body = """
				{"email": "%s", "password": "%s"}
				""".formatted(EMAIL_ADMIN, PASSWORD_ADMIN);

		mockMvc.perform(post("/auth/login-admin").contentType(MediaType.APPLICATION_JSON).content(body))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.token").isNotEmpty())
				.andExpect(jsonPath("$.usuario.rol").value("admin"));
	}

	/**
	 * Decisión de {@code AuthService.loginAdmin}: no distingue en el response
	 * si las credenciales eran incorrectas o si el usuario existe pero no es
	 * admin — un académico con password correcta igual recibe 401 genérico
	 * (nunca 403, para no confirmarle a quien ataca que el usuario existe).
	 */
	@Test
	void loginAdminConCredencialesDeAcademicoDevuelve401() throws Exception {
		String body = """
				{"email": "%s", "password": "%s"}
				""".formatted(EMAIL_ACADEMICO, PASSWORD_ACADEMICO);

		mockMvc.perform(post("/auth/login-admin").contentType(MediaType.APPLICATION_JSON).content(body))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
	}

	@Test
	void registroCreaUnAcademicoNuevo() throws Exception {
		String body = """
				{"nombre": "Nueva", "apellido": "Persona", "email": "nueva.persona.test@ufro.cl", "password": "password123"}
				""";

		mockMvc.perform(post("/auth/registro").contentType(MediaType.APPLICATION_JSON).content(body))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.token").isNotEmpty())
				.andExpect(jsonPath("$.usuario.email").value("nueva.persona.test@ufro.cl"))
				.andExpect(jsonPath("$.usuario.rol").value("academico"));
	}

	@Test
	void registroConEmailYaRegistradoDevuelve409() throws Exception {
		String body = """
				{"nombre": "Tefi", "apellido": "Duplicada", "email": "%s", "password": "password123"}
				""".formatted(EMAIL_ACADEMICO);

		mockMvc.perform(post("/auth/registro").contentType(MediaType.APPLICATION_JSON).content(body))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("EMAIL_TAKEN"));
	}

	@Test
	void registroConPasswordCortaDevuelve400() throws Exception {
		String body = """
				{"nombre": "Alguien", "apellido": "Corto", "email": "corto.test@ufro.cl", "password": "1234567"}
				""";

		mockMvc.perform(post("/auth/registro").contentType(MediaType.APPLICATION_JSON).content(body))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
	}

	@Test
	void logoutSinTokenDevuelve401() throws Exception {
		mockMvc.perform(post("/auth/logout"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
	}

	@Test
	void logoutConTokenInvalidoDevuelve401() throws Exception {
		mockMvc.perform(post("/auth/logout").header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + "esto-no-es-un-jwt"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
	}

	/**
	 * El hallazgo que este test cierra: la invalidación de logout debe
	 * reforzarse server-side vía {@code TokenInvalidadoRepository} (consultado
	 * por {@code jti} en cada request, ver JwtAuthenticationFilter) — no basta
	 * con que el frontend borre el token de su lado.
	 */
	@Test
	void logoutInvalidaElTokenParaRequestsFuturas() throws Exception {
		String token = tokenPara(EMAIL_ACADEMICO);

		mockMvc.perform(get("/historial").header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + token))
				.andExpect(status().isOk());

		mockMvc.perform(post("/auth/logout").header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.message").value("Sesión cerrada correctamente"));

		mockMvc.perform(get("/historial").header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + token))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
	}

	private String tokenPara(String email) {
		Usuario usuario = usuarioRepository.findByEmail(email).orElseThrow();
		return jwtService.generarToken(usuario);
	}
}
