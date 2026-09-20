package com.kimngeam.backend.admin.dashboard.dto;

/**
 * Shape distinto del {@code usuario} de {@code GET /admin/validaciones} (ese
 * sí trae {@code id}, ver {@code admin.validaciones.dto.UsuarioResumenResponse})
 * — el contrato de {@code ultimas_validaciones} en el dashboard solo trae
 * {@code nombre} e {@code inicial}.
 */
public record UsuarioBreveResponse(String nombre, String inicial) {
}
