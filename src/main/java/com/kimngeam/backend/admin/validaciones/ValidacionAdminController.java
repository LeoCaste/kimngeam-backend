package com.kimngeam.backend.admin.validaciones;

import com.kimngeam.backend.admin.validaciones.dto.ValidacionesAdminResponse;
import com.kimngeam.backend.auth.dto.MessageResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Requiere rol {@code admin} (ver {@code SecurityConfig}, matcher
 * {@code /admin/**} desde Fase 1).
 */
@RestController
@RequestMapping("/admin/validaciones")
public class ValidacionAdminController {

	private final ValidacionAdminService validacionAdminService;

	public ValidacionAdminController(ValidacionAdminService validacionAdminService) {
		this.validacionAdminService = validacionAdminService;
	}

	@GetMapping
	public ResponseEntity<ValidacionesAdminResponse> listar(@RequestParam(required = false) String tipo,
			@RequestParam(name = "usuario_id", required = false) Long usuarioId,
			@RequestParam(required = false) String busqueda) {
		return ResponseEntity.ok(validacionAdminService.listar(tipo, usuarioId, busqueda));
	}

	/**
	 * No es parte de los 14 endpoints del contrato original: expone el
	 * mecanismo de reversibilidad de Fase 4 ({@code ValidacionCorpusIndexer.revertir},
	 * que existía pero no tenía forma de invocarse — ver CLAUDE.md, Fase 5).
	 */
	@PostMapping("/{id}/revertir")
	public ResponseEntity<MessageResponse> revertir(@PathVariable String id) {
		return ResponseEntity.ok(validacionAdminService.revertir(id));
	}
}
