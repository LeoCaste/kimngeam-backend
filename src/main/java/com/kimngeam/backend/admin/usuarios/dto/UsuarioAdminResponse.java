package com.kimngeam.backend.admin.usuarios.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;

public record UsuarioAdminResponse(Long id, String nombre, String email, String inicial, long traducciones,
		long validaciones, @JsonProperty("ultimo_acceso") OffsetDateTime ultimoAcceso, String estado) {
}
