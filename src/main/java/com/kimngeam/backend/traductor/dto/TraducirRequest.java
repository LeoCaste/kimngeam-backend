package com.kimngeam.backend.traductor.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * El máximo de entrada configurado ({@code kimngeam.traductor.segmentacion.maximo-entrada})
 * no se valida acá con una anotación: es un umbral configurable en runtime,
 * no una constante de compilación, así que lo revisa {@code TraductorService}
 * al entrar (ver CLAUDE.md, "nada hardcodeado").
 */
public record TraducirRequest(
		@NotBlank(message = "El texto es obligatorio") String texto,
		@NotBlank(message = "La dirección es obligatoria")
		@Pattern(regexp = "es-map|map-es", message = "La dirección debe ser \"es-map\" o \"map-es\"") String direccion) {
}
