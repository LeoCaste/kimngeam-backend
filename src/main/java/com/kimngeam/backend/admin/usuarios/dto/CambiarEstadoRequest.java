package com.kimngeam.backend.admin.usuarios.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Solo acepta {@code "activo"}/{@code "inactivo"} — {@code "sin-actividad"}
 * es derivado en {@code GET /admin/usuarios}, nunca un valor asignable acá.
 */
public record CambiarEstadoRequest(
		@NotBlank(message = "El estado es obligatorio")
		@Pattern(regexp = "activo|inactivo", message = "El estado debe ser \"activo\" o \"inactivo\"") String estado) {
}
