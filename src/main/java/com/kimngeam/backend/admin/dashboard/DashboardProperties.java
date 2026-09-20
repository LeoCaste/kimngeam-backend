package com.kimngeam.backend.admin.dashboard;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Cuántas filas trae {@code ultimas_validaciones} en {@code GET /admin/dashboard}.
 * El contrato no fija un número; se deja configurable en vez de hardcodeado,
 * mismo criterio que {@code UmbralActividadProperties.validacionesRecientesLimite}.
 */
@ConfigurationProperties(prefix = "kimngeam.admin.dashboard")
public record DashboardProperties(@DefaultValue("5") int ultimasValidacionesLimite) {
}
