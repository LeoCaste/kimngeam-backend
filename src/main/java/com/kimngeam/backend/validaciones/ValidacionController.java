package com.kimngeam.backend.validaciones;

import com.kimngeam.backend.shared.security.AuthenticatedUsuario;
import com.kimngeam.backend.validaciones.dto.ValidacionResponse;
import com.kimngeam.backend.validaciones.dto.ValidarExpresionRequest;
import com.kimngeam.backend.validaciones.dto.ValidarGeneralRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Requiere auth + rol {@code academico} (ver {@code SecurityConfig}, ya
 * exigía {@code hasRole("ACADEMICO")} sobre {@code /validaciones/**} desde
 * Fase 1): {@code usuarioAutenticado} nunca es {@code null} acá.
 */
@RestController
@RequestMapping("/validaciones")
public class ValidacionController {

	private final ValidacionService validacionService;

	public ValidacionController(ValidacionService validacionService) {
		this.validacionService = validacionService;
	}

	@PostMapping("/expresion")
	public ResponseEntity<ValidacionResponse> expresion(@Valid @RequestBody ValidarExpresionRequest request,
			@AuthenticationPrincipal AuthenticatedUsuario usuarioAutenticado) {
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(validacionService.registrarExpresion(request, usuarioAutenticado.id()));
	}

	@PostMapping("/general")
	public ResponseEntity<ValidacionResponse> general(@Valid @RequestBody ValidarGeneralRequest request,
			@AuthenticationPrincipal AuthenticatedUsuario usuarioAutenticado) {
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(validacionService.registrarGeneral(request, usuarioAutenticado.id()));
	}
}
