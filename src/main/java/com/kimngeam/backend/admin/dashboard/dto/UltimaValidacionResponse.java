package com.kimngeam.backend.admin.dashboard.dto;

import java.time.OffsetDateTime;

public record UltimaValidacionResponse(String id, UsuarioBreveResponse usuario, String expresion, String texto,
		String tipo, String variante, OffsetDateTime fecha) {
}
