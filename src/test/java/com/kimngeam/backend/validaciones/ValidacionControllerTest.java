package com.kimngeam.backend.validaciones;

import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.kimngeam.backend.shared.entity.Direccion;
import com.kimngeam.backend.shared.entity.Traduccion;
import com.kimngeam.backend.shared.entity.TraduccionRepository;
import com.kimngeam.backend.shared.entity.Usuario;
import com.kimngeam.backend.shared.entity.UsuarioRepository;
import com.kimngeam.backend.shared.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Casos de error del contrato para {@code POST /validaciones/expresion} y
 * {@code POST /validaciones/general} (ver docs/API_CONTRACTS.md). El caso
 * feliz sí dispara el indexado real al corpus (Ollama, local y gratis — ver
 * ValidacionCorpusIndexer), así que corre en el build normal sin costo.
 * {@code @Transactional} para que Spring haga rollback de la validación y del
 * corpus_chunk que genera al final de cada test.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ValidacionControllerTest {

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

	private String traduccionId;
	private String tokenAcademico;
	private String tokenAdmin;

	@BeforeEach
	void setUp() {
		Traduccion traduccion = traduccionRepository
				.save(new Traduccion(null, "texto de origen", "texto traducido", Direccion.ES_MAP, null, null));
		traduccionId = traduccion.getId();
		tokenAcademico = tokenPara(EMAIL_ACADEMICO);
		tokenAdmin = tokenPara(EMAIL_ADMIN);
	}

	@Test
	void adminRecibe403AlValidarExpresion() throws Exception {
		String body = """
				{"traduccion_id": "%s", "expresion": "mapuche mew", "comentario": "correcto"}
				""".formatted(traduccionId);

		mockMvc.perform(post("/validaciones/expresion").contentType(MediaType.APPLICATION_JSON)
				.header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + tokenAdmin)
				.content(body))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.code").value("FORBIDDEN"));
	}

	@Test
	void sinTokenRecibe401() throws Exception {
		String body = """
				{"traduccion_id": "%s", "comentario": "correcto"}
				""".formatted(traduccionId);

		mockMvc.perform(post("/validaciones/general").contentType(MediaType.APPLICATION_JSON).content(body))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
	}

	@Test
	void camposFaltantesRecibe400() throws Exception {
		String body = """
				{"traduccion_id": "%s"}
				""".formatted(traduccionId);

		mockMvc.perform(post("/validaciones/expresion").contentType(MediaType.APPLICATION_JSON)
				.header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + tokenAcademico)
				.content(body))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
	}

	@Test
	void academicoPuedeRegistrarValidacionDeExpresion() throws Exception {
		String body = """
				{"traduccion_id": "%s", "expresion": "mapuche mew", "comentario": "El uso de \\"mew\\" como locativo es correcto.", "variante": "Nguluche"}
				""".formatted(traduccionId);

		mockMvc.perform(post("/validaciones/expresion").contentType(MediaType.APPLICATION_JSON)
				.header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + tokenAcademico)
				.content(body))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").value(startsWith("val_")))
				.andExpect(jsonPath("$.message").value("Validación registrada correctamente"));
	}

	@Test
	void academicoPuedeRegistrarValidacionGeneral() throws Exception {
		String body = """
				{"traduccion_id": "%s", "comentario": "La traducción es adecuada en términos generales."}
				""".formatted(traduccionId);

		mockMvc.perform(post("/validaciones/general").contentType(MediaType.APPLICATION_JSON)
				.header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + tokenAcademico)
				.content(body))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").value(startsWith("val_")))
				.andExpect(jsonPath("$.message").value("Validación general registrada correctamente"));
	}

	private String tokenPara(String email) {
		Usuario usuario = usuarioRepository.findByEmail(email).orElseThrow();
		return jwtService.generarToken(usuario);
	}
}
