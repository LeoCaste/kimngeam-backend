package com.kimngeam.backend.traductor;

import com.kimngeam.backend.shared.entity.Direccion;
import com.kimngeam.backend.shared.security.AuthenticatedUsuario;
import com.kimngeam.backend.traductor.dto.TraducirRequest;
import com.kimngeam.backend.traductor.dto.TraducirResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Auth opcional (ver {@code SecurityConfig}): {@code usuarioAutenticado} es
 * {@code null} para requests anónimas — el filtro JWT nunca rechaza la
 * request cuando falta el token, solo la deja sin autenticar.
 */
@RestController
@RequestMapping("/traductor")
public class TraductorController {

	private final TraductorService traductorService;

	public TraductorController(TraductorService traductorService) {
		this.traductorService = traductorService;
	}

	@PostMapping("/traducir")
	public ResponseEntity<TraducirResponse> traducir(@Valid @RequestBody TraducirRequest request,
			@AuthenticationPrincipal AuthenticatedUsuario usuarioAutenticado) {
		Long usuarioId = usuarioAutenticado == null ? null : usuarioAutenticado.id();
		Direccion direccion = Direccion.fromValue(request.direccion());
		TraduccionResultado resultado = traductorService.traducir(request.texto(), direccion, usuarioId);
		return ResponseEntity.ok(TraducirResponse.desde(resultado));
	}
}
