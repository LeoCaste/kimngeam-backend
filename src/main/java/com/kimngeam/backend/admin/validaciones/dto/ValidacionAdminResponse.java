package com.kimngeam.backend.admin.validaciones.dto;

import java.time.OffsetDateTime;

public record ValidacionAdminResponse(String id, UsuarioResumenResponse usuario, String expresion, String texto,
		String tipo, String variante, OffsetDateTime fecha) {
}
