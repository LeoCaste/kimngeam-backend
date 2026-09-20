package com.kimngeam.backend.admin.usuarios.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record UsuarioDetalleResponse(UsuarioAdminResponse usuario,
		@JsonProperty("validaciones_recientes") List<ValidacionRecienteResponse> validacionesRecientes) {
}
