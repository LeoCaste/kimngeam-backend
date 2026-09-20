package com.kimngeam.backend.admin.usuarios.dto;

import java.time.OffsetDateTime;

public record ValidacionRecienteResponse(String id, String expresion, String texto, String tipo, String variante,
		OffsetDateTime fecha) {
}
