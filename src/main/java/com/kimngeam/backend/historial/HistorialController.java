package com.kimngeam.backend.historial;

import com.kimngeam.backend.historial.dto.HistorialResponse;
import com.kimngeam.backend.shared.security.AuthenticatedUsuario;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Requiere auth + rol {@code academico} (ver {@code SecurityConfig}):
 * {@code usuarioAutenticado} nunca es {@code null} acá.
 */
@RestController
@RequestMapping("/historial")
public class HistorialController {

	private final HistorialService historialService;

	public HistorialController(HistorialService historialService) {
		this.historialService = historialService;
	}

	@GetMapping
	public ResponseEntity<HistorialResponse> historial(@AuthenticationPrincipal AuthenticatedUsuario usuarioAutenticado) {
		return ResponseEntity.ok(historialService.obtenerHistorial(usuarioAutenticado.id()));
	}
}
