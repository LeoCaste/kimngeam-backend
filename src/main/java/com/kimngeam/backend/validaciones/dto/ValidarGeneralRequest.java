package com.kimngeam.backend.validaciones.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public record ValidarGeneralRequest(
		@JsonProperty("traduccion_id") @NotBlank(message = "traduccion_id es obligatorio") String traduccionId,
		@NotBlank(message = "El comentario es obligatorio") String comentario) {
}
