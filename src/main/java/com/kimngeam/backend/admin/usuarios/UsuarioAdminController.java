package com.kimngeam.backend.admin.usuarios;

import com.kimngeam.backend.admin.usuarios.dto.CambiarEstadoRequest;
import com.kimngeam.backend.admin.usuarios.dto.InvitarUsuarioRequest;
import com.kimngeam.backend.admin.usuarios.dto.UsuarioDetalleResponse;
import com.kimngeam.backend.admin.usuarios.dto.UsuariosAdminResponse;
import com.kimngeam.backend.auth.dto.MessageResponse;
import com.kimngeam.backend.shared.security.AuthenticatedUsuario;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Requiere rol {@code admin} — ya lo exigía {@code SecurityConfig} desde
 * Fase 1 (matcher {@code /admin/**}); {@code usuarioAutenticado} nunca es
 * {@code null} acá.
 */
@RestController
@RequestMapping("/admin/usuarios")
public class UsuarioAdminController {

	private final UsuarioAdminService usuarioAdminService;

	public UsuarioAdminController(UsuarioAdminService usuarioAdminService) {
		this.usuarioAdminService = usuarioAdminService;
	}

	@GetMapping
	public ResponseEntity<UsuariosAdminResponse> listar() {
		return ResponseEntity.ok(usuarioAdminService.listar());
	}

	@GetMapping("/{id}")
	public ResponseEntity<UsuarioDetalleResponse> detalle(@PathVariable Long id) {
		return ResponseEntity.ok(usuarioAdminService.obtenerDetalle(id));
	}

	@PostMapping("/invitar")
	public ResponseEntity<MessageResponse> invitar(@Valid @RequestBody InvitarUsuarioRequest request,
			@AuthenticationPrincipal AuthenticatedUsuario usuarioAutenticado) {
		return ResponseEntity.ok(usuarioAdminService.invitar(request, usuarioAutenticado.id()));
	}

	@PatchMapping("/{id}/estado")
	public ResponseEntity<MessageResponse> cambiarEstado(@PathVariable Long id,
			@Valid @RequestBody CambiarEstadoRequest request) {
		return ResponseEntity.ok(usuarioAdminService.cambiarEstado(id, request));
	}
}
